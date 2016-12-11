package com.johan.jsqlite.processor;

import java.io.IOException;
import java.io.Writer;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import com.johan.jsqlite.JSQLiteDao;
import com.johan.jsqlite.Utils;
import com.johan.jsqlite.annotation.Column;
import com.johan.jsqlite.annotation.Table;

@SupportedAnnotationTypes("com.johan.jsqlite.annotation.*")  
@SupportedSourceVersion(SourceVersion.RELEASE_8)  
public class DaoProcessor extends AbstractProcessor {
	
	private List<DaoElement> daoElementList = new ArrayList<DaoElement>();
	
	private Filer filer;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		filer = processingEnvironment.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
		
		// parse Table annotation
		for (Element element : roundEnvironment.getElementsAnnotatedWith(Table.class)) {
			if (element.getKind() == ElementKind.CLASS) {
				TypeElement classElement = (TypeElement) element;
				PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
				String qualifiedName = classElement.getQualifiedName().toString();
				String packageName = packageElement.getQualifiedName().toString();
				String simpleName = classElement.getSimpleName().toString();
				String tableName = classElement.getAnnotation(Table.class).value();
				DaoElement daoElement = new DaoElement(qualifiedName, packageName, simpleName, tableName);
				daoElementList.add(daoElement);
			}
		}
		
		// parse Column annotation
		for (Element element : roundEnvironment.getElementsAnnotatedWith(Column.class)) {
			if (element.getKind() == ElementKind.FIELD) {
				VariableElement fieldElement = (VariableElement) element;
				TypeElement classElement = (TypeElement) fieldElement.getEnclosingElement();
				String qualifiedName = classElement.getQualifiedName().toString();
				DaoElement daoElement = finDaoElementByQualifiedName(qualifiedName);
				if (daoElement == null) {
					continue;
				}
				Column column = fieldElement.getAnnotation(Column.class);
				String columnName = column.value();
				boolean columnCanNull = column.canNull();
				String columnType = fieldElement.asType().toString();
				DaoColumnElement columnElement = new DaoColumnElement(fieldElement.getSimpleName().toString(), columnName, columnType, columnCanNull);
				daoElement.addColnmnElement(columnElement);
			}
		}
		
		// auto create TableConfig.java for configure table
		try {
			createTableConfigCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// auto create ModelDao.java for every model
		for (DaoElement daoElement : daoElementList) {
			try {
				generateCode(daoElement);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return true;
		
	}
	
	private void createTableConfigCode() throws IOException {
		String javaPackage = "com.johan.jsqlite.config";
		String javaName = "TableConfig";
		JavaFileObject javaFileObject = filer.createSourceFile(javaPackage + "." + javaName);
		Writer writer = javaFileObject.openWriter();
		StringBuilder javaContent = new StringBuilder();
		// package
		javaContent.append("package ").append(javaPackage).append(";").append("\r\n\n");
		// import
		javaContent.append("import java.util.List;").append("\r\n");
		javaContent.append("import java.util.ArrayList;").append("\r\n\n");
		// class begin
		javaContent.append("public class ").append(javaName).append(" {").append("\r\n\n");
		// define method begin
		javaContent.append("    public List<String> getTableCreateSQLList() {").append("\r\n")
				   .append("        List<String> list = new ArrayList<String>();").append("\r\n");
		StringBuilder tableCreateSQL;
		for (DaoElement daoElement : daoElementList) {
			tableCreateSQL = new StringBuilder();
			tableCreateSQL.append("CREATE TABLE IF NOT EXISTS ").append(daoElement.getTableName()).append(" (");
			tableCreateSQL.append(JSQLiteDao.KEY_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
			for (DaoColumnElement columnElement : daoElement.getColnmnElementList()) {
				tableCreateSQL.append(columnElement.getColumnName()).append(" ")
						      .append(Utils.getColumnTypeFromFieldType(columnElement.getColumnType())).append(" ");
				if (!columnElement.isCanNull()) {
					tableCreateSQL.append("NOT NULL");
				}
				tableCreateSQL.append(", ");
			}
			tableCreateSQL.deleteCharAt(tableCreateSQL.length()-1);
			tableCreateSQL.deleteCharAt(tableCreateSQL.length()-1);
			tableCreateSQL.append(");");
			javaContent.append("        list.add(\"").append(tableCreateSQL.toString()).append("\");").append("\r\n");
		}
		javaContent.append("        return list;").append("\r\n");
		// define method end
		javaContent.append("    }").append("\r\n\n");
		// class end
		javaContent.append("}");
		// write to file
		writer.write(javaContent.toString());
		writer.flush();
		writer.close();
	}
	
	private void generateCode(DaoElement daoElement) throws IOException {
		String daoPackage = "com.johan.jsqlite.dao";
		String daoSimpleName = daoElement.getSimpleName() + "Dao";
		List<DaoColumnElement> columnElementList = daoElement.getColnmnElementList();
		JavaFileObject javaFileObject = filer.createSourceFile(daoPackage+"."+daoSimpleName);
		Writer writer = javaFileObject.openWriter();
		StringBuilder javaContent = new StringBuilder();
		// package
		javaContent.append("package ").append(daoPackage).append(";").append("\r\n\n");
		// import
		javaContent.append("import com.johan.jsqlite.JSQLiteDao;").append("\r\n")
				   .append("import ").append(daoElement.getQualifiedName()).append(";").append("\r\n\n")
				   .append("import android.content.ContentValues;").append("\r\n")
				   .append("import android.database.Cursor;").append("\r\n\n");
		// class begin
		javaContent.append("public class ").append(daoSimpleName).append(" extends JSQLiteDao<").append(daoElement.getSimpleName()).append("> {").append("\r\n\n");
		// define field
		for(DaoColumnElement columnElement : columnElementList) {
			javaContent.append("    public static String ").append(columnElement.getColumnKey()).append(" = \"").append(columnElement.getColumnName()).append("\";").append("\r\n");
		}
		javaContent.append("\n");
		// override cursorToData method
		javaContent.append("    @Override").append("\r\n")
				   .append("    public ").append(daoElement.getSimpleName()).append(" cursorToData(Cursor cursor) {").append("\r\n")
				   .append("        ").append(daoElement.getSimpleName()).append(" data = new ").append(daoElement.getSimpleName()).append("();").append("\r\n");
		javaContent.append("        data.setId(cursor.getInt(cursor.getColumnIndex(\"id\")));").append("\r\n");
		for(DaoColumnElement columnElement : columnElementList) {
			javaContent.append("        ").append("data.").append(columnElement.getColumnSetMethod()).append("(").append(Utils.getCursorDataMethod(columnElement.getColumnName(), columnElement.getColumnType())).append(");").append("\r\n");
		}
		javaContent.append("        return data;").append("\r\n");
		javaContent.append("    }").append("\r\n\n");
		// override getContentValues method
		javaContent.append("    @Override").append("\r\n")
				   .append("    public ContentValues getContentValues(").append(daoElement.getSimpleName()).append(" data) {").append("\r\n")
				   .append("        ContentValues contentValues = new ContentValues();").append("\r\n");
		for(DaoColumnElement columnElement : columnElementList) {
			if (columnElement.getColumnType().equals(Date.class.getName())) {
				javaContent.append("        contentValues.put(\"").append(columnElement.getColumnName()).append("\", data.").append(columnElement.getColumnGetMethod()).append("().getTime());").append("\r\n");
			} else if (columnElement.getColumnType().equals(Boolean.class.getName()) || columnElement.getColumnType().equals(boolean.class.getName())) {
				javaContent.append("        contentValues.put(\"").append(columnElement.getColumnName()).append("\", data.").append(columnElement.getColumnGetMethod()).append("() ? 1 : 0);").append("\r\n");
			} else {
				javaContent.append("        contentValues.put(\"").append(columnElement.getColumnName()).append("\", data.").append(columnElement.getColumnGetMethod()).append("());").append("\r\n");
			}
		}
		javaContent.append("        return contentValues;").append("\r\n");
		javaContent.append("    }").append("\r\n\n");
		// override getTableName method
		javaContent.append("    @Override").append("\r\n")
				   .append("    public String getTableName() {").append("\r\n")
				   .append("        return \"").append(daoElement.getTableName()).append("\";").append("\r\n")
				   .append("    }").append("\r\n\n");
		// class end
		javaContent.append("}").append("\r\n");
		// write to file
		writer.write(javaContent.toString());
		writer.flush();
		writer.close();
	}
	
	
	
	private DaoElement finDaoElementByQualifiedName(String qualifiedName) {
		
		for (DaoElement daoElement : daoElementList) {
			if (daoElement.getQualifiedName().equals(qualifiedName)) {
				return daoElement;
			}
		}
		
		return null;
		
	}
	
	public class DaoColumnElement {
		
		private String column;
		private String columnName;
        private String columnType;
        private boolean canNull;
        
		public DaoColumnElement(String column, String columnName, String columnType, boolean canNull) {
			this.column = column;
			this.columnName = Utils.UpAndLowToAllLow("".equals(columnName) ? column : columnName);
			this.columnType = columnType;
			this.canNull = canNull;
		}
		
		public String getColumn() {
			return column;
		}
		
		public String getColumnGetMethod() {
			if (columnType.equals(Boolean.class.getName()) || columnType.equals(boolean.class.getName())) {
				boolean isStartWithIs = column.startsWith("is");
				if (isStartWithIs) {
					char threeChar = column.charAt(2);
					if (Character.isUpperCase(threeChar)) {
						return column;
					} else {
						return "is" + column.substring(0, 1).toUpperCase() + column.substring(1);
					}
				} else {
					return "is" + column.substring(0, 1).toUpperCase() + column.substring(1);
				}
			} else {
				return "get" + column.substring(0, 1).toUpperCase() + column.substring(1);
			}
		}
		
		public String getColumnSetMethod() {
			if (columnType.equals(Boolean.class.getName()) || columnType.equals(boolean.class.getName())) {
				boolean isStartWithIs = column.startsWith("is");
				if (isStartWithIs) {
					char threeChar = column.charAt(2);
					if (Character.isUpperCase(threeChar)) {
						return "set" + column.substring(2);
					} else {
						return "set" + column.substring(0, 1).toUpperCase() + column.substring(1);
					}
				} else {
					return "set" + column.substring(0, 1).toUpperCase() + column.substring(1);
				}
			} else {
				return "set" + column.substring(0, 1).toUpperCase() + column.substring(1);
			}
		}
		
		public String getColumnKey() {
			return "KEY_" + Utils.UpAndLowToAllUp(column);
		}
		
		public String getColumnName() {
			return columnName;
		}
		
		public String getColumnType() {
			return columnType;
		}
		
		public boolean isCanNull() {
			return canNull;
		}
		
	}
	
	public class DaoElement {
		
		private String qualifiedName;
		private String packageName;
		private String simpleName;
		private String tableName;
		private List<DaoColumnElement> colnmnElementList;
		
		public DaoElement(String qualifiedName, String packageName, String simpleName, String tableName) {
			this.qualifiedName = qualifiedName;
			this.packageName = packageName;
			this.simpleName = simpleName;
			this.tableName = "".equals(tableName) ? Utils.UpAndLowToAllLow(simpleName) : tableName;
			colnmnElementList = new ArrayList<DaoColumnElement>();
		}
		
		public String getPackageName() {
			return packageName;
		}

		public String getQualifiedName() {
			return qualifiedName;
		}

		public String getSimpleName() {
			return simpleName;
		}

		public List<DaoColumnElement> getColnmnElementList() {
			return colnmnElementList;
		}

		public void addColnmnElement(DaoColumnElement element) {
			colnmnElementList.add(element);
		}

		public String getTableName() {
			return tableName;
		}
		
	}

}
