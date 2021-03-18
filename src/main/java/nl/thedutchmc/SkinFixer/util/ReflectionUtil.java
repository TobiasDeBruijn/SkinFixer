package nl.thedutchmc.SkinFixer.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

public class ReflectionUtil {

	public static String SERVER_VERSION;
	
	static {
		//Load the Bukit class
		try {
			Class.forName("org.bukkit.Bukkit");
		} catch (ClassNotFoundException ignored) {}
		
		//Get the version String
		SERVER_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 1);
	}
	
	/**
	 * Get a Class from the org.bukkit.craftbukkit.SERVER_VERSION. package
	 * @param className The name of the class
	 * @return Returns the Class
	 * @throws ClassNotFoundException Thrown when the Class was not found
	 */
	public static Class<?> getBukkitClass(String className) throws ClassNotFoundException {
		return Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + "." + className);
	}
	
	/**
	 * Get a Class from the net.minecraft.server.SERVER_VERSION. package
	 * @param className The name of the class
	 * @return Returns the Class
	 * @throws ClassNotFoundException Thrown when the Class was not found
	 */
	public static Class<?> getNmsClass(String className) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + SERVER_VERSION + "." + className);
	}
	
	/**
	 * Get the Constructor of a Class
	 * @param clazz The Class in which the Constructor is defined
	 * @param args Arguments taken by the Constructor
	 * @return Returns the Constructor
	 * @throws NoSuchMethodException Thrown when no Constructor in the Class was found with the provided combination of arguments
	 */
	public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... args) throws NoSuchMethodException {
		Constructor<?> con = clazz.getConstructor(args);
		con.setAccessible(true);
		
		return con;
	}
	
	/**
	 * Get an Enum from an Enum constant
	 * @param clazz The Class in which the Enum is defined
	 * @param constant The name of the Enum Constant
	 * @return Returns the Enum
	 * @throws ClassNotFoundException
	 */
	public static Enum<?> getEnum(Class<?> clazz, String constant) throws ClassNotFoundException {
		Class<?> c = Class.forName(clazz.getName());
		Enum<?>[] constants = (Enum<?>[]) c.getEnumConstants();
		
		for(Enum<?> e : constants) {
			if(e.name().equalsIgnoreCase(constant)) {
				return e;
			}
		}
		
		return null;
	}
	
	/**
	 * Get an Enum constant by it's name and constant
	 * @param clazz The Class in which the Enum is defined
	 * @param enumname The name of the Enum
	 * @param constant The name of the Constant
	 * @return Returns the Enum
	 * @throws ClassNotFoundException
	 */
    public static Enum<?> getEnum(Class<?> clazz, String enumname, String constant) throws ClassNotFoundException {
        Class<?> c = Class.forName(clazz.getName() + "$" + enumname);
        Enum<?>[] econstants = (Enum<?>[]) c.getEnumConstants();
        
        for (Enum<?> e : econstants) {
            if (e.name().equalsIgnoreCase(constant)) {
                return e;
            }
        }

        return null;
    }
    
    /**
     * Get a Field
     * @param clazz The Class in which the Field is defined
     * @param fieldName The name of the Field
     * @return Returns the Field
     * @throws NoSuchFieldException Thrown when the Field was not present in the Class
     */
    public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f;
    }
    
    /**
     * Get a Method
     * @param clazz The Class in which the Method is defined
     * @param methodName The name of the method
     * @param args The argument types the method takes
     * @return Returns the Method
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) throws NoSuchMethodException {
        Method m = clazz.getDeclaredMethod(methodName, args);
        m.setAccessible(true);
        return m;
    }
    
    /**
     * Invoke a Method which takes no arguments. The Class in which the Method is defined is derived from the provided Object
     * @param obj The object to invoke the method on
     * @param methodName The name of the Method
     * @return Returns the result of the method, can be null if the method returns void
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object obj, String methodName) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Method m = getMethod(obj.getClass(), methodName);
    	return m.invoke(obj);
    }
    
    /**
     * Invoke a Method where the argument types are derived from the provided arguments. The Class in which the Method is defined is derived from the provided Object
     * @param obj The object to invoke the method on
     * @param methodName The name of the Method
     * @param args The arguments to pass to the Method
     * @return Returns the result of the method, can be null if the method returns void
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object obj, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	return invokeMethod(obj.getClass(), obj, methodName, args);
    }
    
    /**
     * Invoke a Method where the argument types are explicitly given (Helpful when working with primitives). The Class in which the Method is defined is derived from the provided Object.
     * @param obj The Object to invoke the method on
     * @param methodName The name of the Method
     * @param argTypes The types of arguments as a Class array
     * @param args The arguments as an object array
     * @return Returns the result of the method, can be null if the method returns void
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] argTypes, Object[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Method m = getMethod(obj.getClass(), methodName, argTypes);
    	return m.invoke(obj, args);
    }
    
    /**
     * Invoke a Method where the Class where to find the method is explicitly given (Helpful if the method is located in a superclass). The argument types are derived from the provided arguments
     * @param clazz The Class where the method is located
     * @param obj The Object to invoke the method on
     * @param methodName The name of the method
     * @param args The arguments to be passed to the method
     * @return Returns the result of the method, can be null if the method returns void
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Class<?> clazz, Object obj, String methodName, Object... args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Class<?>[] argTypes = new Class<?>[args.length];
    	for(int i = 0; i < args.length; i++) {
    		argTypes[i] = args[i].getClass();
    	}
    	
    	Method m = getMethod(clazz, methodName, argTypes);
    	
    	return m.invoke(obj, args);
    }
    
    /**
     * Invoke a Method where the Class where the Method is defined is explicitly given, and the argument types are explicitly given
     * @param clazz The Class in which the Method is located
     * @param obj The Object on which to invoke the Method
     * @param methodName The name of the Method
     * @param argTypes Argument types
     * @param args Arguments to pass to the method
     * @return Returns the result of the method, can be null if the method returns void
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Class<?> clazz, Object obj, String methodName, Class<?>[] argTypes, Object[] args) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Method m = getMethod(clazz, methodName, argTypes);
    	return m.invoke(obj, args);
    }
    
    /**
     * Get the value of a Field, where the Class in which the field is defined is derived from the provided Object
     * @param obj The object in which the field is located, and from which to get the value
     * @param name The name of the Field to get the value from
     * @return Returns the value of the Field
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object getObject(Object obj, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    	Field f = getField(obj.getClass(), name);
    	return f.get(obj);
    }
    
    /**
     * Get the value of a Field, where the Class in which the Field is defined is explicitly given. (Helpful when the Field is in a superclass)
     * @param obj The Object to get the value from
     * @param clazz The Class in which the Field is defined
     * @param name The name of the Field
     * @return Returns the value of the Field
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object getObject(Object obj, Class<?> clazz, String name) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    	Field f = getField(clazz, name);
    	return f.get(obj);
    }
    
    /**
     * Invoke a Class' constructor. The argument types are derived from the provided arguments
     * @param clazz The Class in which the Constructor is defined
     * @param args The arguments to pass to the Constructor
     * @return Returns an instance of the provided Class in which the Constructor is located
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeConstructor(Class<?> clazz, Object... args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Class<?>[] argTypes = new Class<?>[args.length];
    	for(int i = 0; i < args.length; i++) {
    		argTypes[i] = args[i].getClass();
    	}
    	
    	Constructor<?> con = getConstructor(clazz, argTypes);
    	
    	return con.newInstance(args);
    }
    
    /**
     * Invoke a Class' Constructor, where the argument types are explicitly given (Helpful when working with primitives)
     * @param clazz The Class in which the Constructor is defined
     * @param argTypes The argument types 
     * @param args The arguments to pass to the constructor
     * @return Returns an instance of the provided Class in which the Constructor is located
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeConstructor(Class<?> clazz, Class<?>[] argTypes, Object[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	Constructor<?> con = getConstructor(clazz, argTypes);
    	return con.newInstance(args);
    }
    
    /**
     * <strong> For debugging purposes! </strong><br>
     * Print all Methods in a Class with their parameters, will print to stdout
     * @param clazz The Class to look in
     */
    public static void printMethodsInClassTyped(Class<?> clazz) {
    	System.out.println("Methods in " + clazz.getName() + ":");
    	
    	for(Method m : clazz.getDeclaredMethods()) {
			String print = m.getName() + "(";
    		for(int i = 0; i < m.getParameterTypes().length; i++) {
    			print += m.getParameterTypes()[i].getName();
    			
    			if(i != m.getParameterTypes().length -1) {
    				print += ",";
    			}

    		}
    		
    		print += ")";
    		System.out.println(print);
    	}
    } 
}