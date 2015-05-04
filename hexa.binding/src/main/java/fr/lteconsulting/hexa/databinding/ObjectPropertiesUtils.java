package fr.lteconsulting.hexa.databinding;

import java.util.logging.Logger;

import fr.lteconsulting.hexa.classinfo.ClassInfo;
import fr.lteconsulting.hexa.classinfo.Clazz;
import fr.lteconsulting.hexa.classinfo.Field;
import fr.lteconsulting.hexa.classinfo.Method;
import fr.lteconsulting.hexa.databinding.propertyadapters.CompositePropertyAdapter;
import fr.lteconsulting.hexa.databinding.tools.Property;

/**
 * Utility class supporting the concept of Property.
 * 
 * A Property on an object is a value that can be get and/or set through either
 * a getter/setter or directly through the object's field.
 * 
 * @author Arnaud Tournier (c) LTE Consulting - 2015 http://www.lteconsulting.fr
 *
 */
public class ObjectPropertiesUtils
{
	private final static Logger LOGGER = Logger.getLogger( ObjectPropertiesUtils.class.getName() );

	private final static PlatformSpecific propertyBagAccess = PlatformSpecificProvider.get();

	/**
	 * Whether a getter or a field is available with that name
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static boolean HasSomethingToGetField( Clazz<?> clazz, String name )
	{
		return GetGetterPropertyType( clazz, name ) != null;
	}

	/**
	 * Returns the class of the property
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static Class<?> GetPropertyType( Clazz<?> clazz, String name )
	{
		Class<?> getterType = GetGetterPropertyType( clazz, name );
		Class<?> setterType = GetSetterPropertyType( clazz, name );

		if( getterType == setterType )
			return getterType;

		return null;
	}

	/**
	 * Return the property getter type
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static Class<?> GetGetterPropertyType( Clazz<?> clazz, String name )
	{
		String getterName = "get" + capitalizeFirstLetter( name );
		Method getter = clazz.getMethod( getterName );
		if( getter != null )
			return getter.getReturnType();

		// try direct field access
		Field field = clazz.getAllField( name );
		if( field != null )
			return field.getType();

		return null;
	}

	/**
	 * Gets the property's value from an object
	 * 
	 * @param object
	 *            The object
	 * @param name
	 *            Property name
	 * @return
	 */
	public static <T> T GetProperty( Object object, String name )
	{
		return GetProperty( object, name, true );
	}

	/**
	 * Gets the property's value from an object
	 * 
	 * @param object
	 *            The object
	 * @param name
	 *            Property name
	 * @param fTryDirectFieldAccess
	 *            specifies if direct field access should be used
	 * @return
	 */
	public static <T> T GetProperty( Object object, String name, boolean fTryDirectFieldAccess )
	{
		T result = GetPropertyImpl( object, name, fTryDirectFieldAccess );
		if( result instanceof Property )
		{
			@SuppressWarnings( "unchecked" )
			Property<T> property = ((Property<T>) result);
			return property.getValue();
		}

		return result;
	}

	/**
	 * Whether there is a setter or a field to write this property
	 */
	public static boolean HasSomethingToSetField( Clazz<?> clazz, String name )
	{
		return GetSetterPropertyType( clazz, name ) != null;
	}

	/**
	 * Returns the class of the setter property. It can be this of the setter or
	 * of the field
	 */
	public static Class<?> GetSetterPropertyType( Clazz<?> clazz, String name )
	{
		String setterName = "set" + capitalizeFirstLetter( name );
		Method setter = clazz.getMethod( setterName );
		if( setter != null && setter.getParameterTypes().size() == 1 )
			return setter.getParameterTypes().get( 0 );

		Field field = clazz.getAllField( name );
		if( field != null )
			return field.getType();

		return null;
	}

	/**
	 * Sets a value on an object's property
	 */
	public static boolean SetProperty( Object object, String propertyName, Object value )
	{
		return SetProperty( object, propertyName, value, true );
	}

	/**
	 * Sets a value on an object's property
	 */
	public static boolean SetProperty( Object object, String propertyName, Object value, boolean fTryDirectFieldAccess )
	{
		Clazz<?> s = ClassInfo.Clazz( object.getClass() );

		if( Property.class == GetPropertyType( s, propertyName ) )
		{
			@SuppressWarnings( "unchecked" )
			Property<Object> property = (Property<Object>) GetPropertyImpl( object, propertyName, fTryDirectFieldAccess );
			if( property != null )
			{
				property.setValue( value );
				return true;
			}
			return false;
		}

		return SetPropertyImpl( s, object, propertyName, value, fTryDirectFieldAccess );
	}

	/**
	 * Gets a dynamic property value on an object
	 */
	public static <T> T GetObjectDynamicProperty( Object object, String propertyName )
	{
		DynamicPropertyBag bag = propertyBagAccess.getObjectDynamicPropertyBag( object );
		if( bag == null )
			return null;

		@SuppressWarnings( "unchecked" )
		T result = (T) bag.get( propertyName );

		return result;
	}

	/**
	 * Whether a dynamic property value has already been set on this object
	 */
	public static boolean HasObjectDynamicProperty( Object object, String propertyName )
	{
		DynamicPropertyBag bag = propertyBagAccess.getObjectDynamicPropertyBag( object );
		if( bag == null )
			return false;

		return bag.contains( propertyName );
	}

	/**
	 * Sets a dynamic property value on an object.
	 */
	public static void SetObjectDynamicProperty( Object object, String propertyName, Object value )
	{
		DynamicPropertyBag bag = propertyBagAccess.getObjectDynamicPropertyBag( object );
		if( bag == null )
		{
			bag = new DynamicPropertyBag();
			propertyBagAccess.setObjectDynamicPropertyBag( object, bag );
		}

		bag.set( propertyName, value );

		NotifyPropertyChangedEvent.notify( object, propertyName );
	}

	private static <T> T GetPropertyImpl( Object object, String name, boolean fTryDirectFieldAccess )
	{
		if( PlatformSpecificProvider.get().isBindingToken( name ) )
		{
			return PlatformSpecificProvider.get().getBindingValue( object, name );
		}

		if( name.equals( CompositePropertyAdapter.DTOMAP_TOKEN ) )
			throw new RuntimeException( "Property of type $DTOMap cannot be readden !" );

		// if has dynamic-property, return it !
		if( HasObjectDynamicProperty( object, name ) )
		{
			LOGGER.fine( "'" + name + "' read dynamic property on object " + object );
			return GetObjectDynamicProperty( object, name );
		}

		Clazz<?> s = ClassInfo.Clazz( object.getClass() );

		String getterName = "get" + capitalizeFirstLetter( name );
		Method getter = s.getMethod( getterName );
		if( getter != null )
		{
			try
			{
				@SuppressWarnings( "unchecked" )
				T result = (T) getter.invoke( object );
				return result;
			}
			catch( Exception e )
			{
				throw new RuntimeException( "ObjectAdapter [object]." + object.getClass().getName() + "." + getterName + "() : getter call throwed an exception. See cause.", e );
			}
		}

		if( fTryDirectFieldAccess )
		{
			// try direct field access
			Field field = s.getAllField( name );
			if( field != null )
				return field.getValue( object );
		}

		// Maybe a dynamic property will be set later on
		LOGGER.warning( "DataBinding: Warning: assuming that the object would in the future have a dynamic property set / Maybe have an opt-in option on the Binding to clarify things" );

		return null;
	}

	private static boolean SetPropertyImpl( Clazz<?> s, Object object, String name, Object value, boolean fTryDirectFieldAccess )
	{
		if( PlatformSpecificProvider.get().isBindingToken( name ) )
		{
			return PlatformSpecificProvider.get().setBindingValue( object, name, value );
		}

		String setterName = "set" + capitalizeFirstLetter( name );
		Method setter = s.getMethod( setterName );
		if( setter != null )
		{
			if( setter.getParameterTypes().get( 0 ) == Property.class )
			{
			}
			setter.invoke( object, value );
			return true;
		}

		if( fTryDirectFieldAccess )
		{
			Field field = s.getAllField( name );
			if( field != null )
			{
				field.setValue( object, value );
				return true;
			}
		}

		if( !HasObjectDynamicProperty( object, name ) )
			LOGGER.warning( "'" + name + "' write dynamic property on object " + object.getClass().getName() + " with value " + value + " WARNING : THAT MEANS THERE IS NO GETTER/SETTER/FIELD FOR THAT CLASS ! PLEASE CHECK THAT IT IS REALLY INTENTIONAL !" );

		SetObjectDynamicProperty( object, name, value );

		return false;
	}

	private static String capitalizeFirstLetter( String s )
	{
		return s.substring( 0, 1 ).toUpperCase() + s.substring( 1 );
	}
}