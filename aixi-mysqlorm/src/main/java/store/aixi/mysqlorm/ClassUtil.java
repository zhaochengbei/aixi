package store.aixi.mysqlorm;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * author：zhaochengbei
 * date：2017/8/15
*/
public class ClassUtil {

	/**
	 * 
	 * @param pkgName
	 * @param isRecursive
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public static List<Class<?>> getClassList(String pkgName) throws IOException, ClassNotFoundException{  
        List<Class<?>> classList = new ArrayList<Class<?>>();  
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String strFile = pkgName.replaceAll("\\.", "/");  
        Enumeration<URL> urls = loader.getResources(strFile);  
        while (urls.hasMoreElements()) {  
            URL url = urls.nextElement();  
            if (url != null) {  
                String protocol = url.getProtocol();  
                String pkgPath = url.getPath();  
                System.out.println("protocol:" + protocol +" path:" + pkgPath);  
                if ("file".equals(protocol)) {  
                    //
                    findClassName(classList, pkgName, pkgPath);  
                } else if ("jar".equals(protocol)) {  
                    // 引用第三方jar的代码  
                    findClassName(classList, pkgName, url);  
                }  
            }  
        }  
          
        return classList;  
    }  
      
    public static void findClassName(List<Class<?>> clazzList, String pkgName, String pkgPath) throws ClassNotFoundException {  
        if(clazzList == null){  
            return;  
        }  
        File[] files = filterClassFiles(pkgPath);// 过滤出.class文件及文件夹  
        System.out.println("files:" +((files == null)?"null" : "length=" + files.length));  
        if(files != null){  
            for (File f : files) {  
                String fileName = f.getName();  
                if (f.isFile()) {  
                    // .class 文件的情况  
                    String clazzName = getClassName(pkgName, fileName);  
                    addClassName(clazzList, clazzName);  
                } else {  
                    // 需要继续查找该文件夹/包名下的类  
                    String subPkgName = pkgName +"."+ fileName;  
                    String subPkgPath = pkgPath +"/"+ fileName;  
                    findClassName(clazzList, subPkgName, subPkgPath); 
                }  
            }  
        }  
    }  
      
    /** 
     * 第三方Jar类库的引用。<br/> 
     * @throws IOException  
     * @throws ClassNotFoundException 
     * */  
    public static void findClassName(List<Class<?>> clazzList, String pkgName, URL url) throws IOException, ClassNotFoundException {  
        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();  
        JarFile jarFile = jarURLConnection.getJarFile();  
        System.out.println("jarFile:" + jarFile.getName());  
        Enumeration<JarEntry> jarEntries = jarFile.entries();  
        while (jarEntries.hasMoreElements()) {  
            JarEntry jarEntry = jarEntries.nextElement();  
            String jarEntryName = jarEntry.getName(); // 类似：sun/security/internal/interfaces/TlsMasterSecret.class  
            String clazzName = jarEntryName.replace("/", ".");  
            int endIndex = clazzName.lastIndexOf(".");  
            String prefix = null;  
            if (endIndex > 0) {  
                clazzName = clazzName.substring(0, endIndex);  
                endIndex = clazzName.lastIndexOf(".");  
                if(endIndex > 0){  
                    prefix = clazzName.substring(0, endIndex);  
                }  
            }  
            if (prefix != null && jarEntryName.endsWith(".class")) {    
                if(prefix.startsWith(pkgName)){  
                    addClassName(clazzList, clazzName);  
                }  
            }  
        }  
    }  
      
    private static File[] filterClassFiles(String pkgPath) {  
        if(pkgPath == null){  
            return null;  
        }  
        return new File(pkgPath).listFiles(new FileFilter() {
            public boolean accept(File file) {  
                return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();  
            }  
        });  
    }  
      
    private static String getClassName(String pkgName, String fileName) {  
        int endIndex = fileName.lastIndexOf(".");  
        String clazz = null;  
        if (endIndex >= 0) {  
            clazz = fileName.substring(0, endIndex);  
        }  
        String clazzName = null;  
        if (clazz != null) {  
            clazzName = pkgName + "." + clazz;  
        }  
        return clazzName;  
    }  
      
    private static void addClassName(List<Class<?>> clazzList, String clazzName)throws ClassNotFoundException {  
        if (clazzList != null && clazzName != null) {  
            Class<?> clazz = null;
//            if("org.apache.log4j.Priority".equals(clazzName)){
            	System.out.println(clazzName);
//            }
            	
            clazz = Class.forName(clazzName);
            clazzList.add(clazz);  
        }  
    }  
}
