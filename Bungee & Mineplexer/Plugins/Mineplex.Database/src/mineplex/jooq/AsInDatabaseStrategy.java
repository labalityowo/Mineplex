package mineplex.jooq;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.jooq.util.DefaultGeneratorStrategy;
import org.jooq.util.Definition;

/**
 * It is recommended that you extend the DefaultGeneratorStrategy. Most of the
 * GeneratorStrategy API is already declared final. You only need to override any
 * of the following methods, for whatever generation behaviour you'd like to achieve
 * <p/>
 * Beware that most methods also receive a "Mode" object, to tell you whether a
 * TableDefinition is being rendered as a Table, Record, POJO, etc. Depending on
 * that information, you can add a suffix only for TableRecords, not for Tables
 */
public class AsInDatabaseStrategy extends DefaultGeneratorStrategy
{
	/**
	 * Override this to specify what identifiers in Java should look like.
	 * This will just take the identifier as defined in the database.
	 */
	@Override
	public String getJavaIdentifier(Definition definition)
	{
		return definition.getOutputName();
	}

	/**
	 * Override these to specify what a setter in Java should look like. Setters
	 * are used in TableRecords, UDTRecords, and POJOs. This example will name
	 * setters "set[NAME_IN_DATABASE]"
	 */
	@Override
	public String getJavaSetterName(Definition definition, Mode mode)
	{
		return "set" + definition.getOutputName().substring(0, 1).toUpperCase() + definition.getOutputName().substring(1);
	}

	/**
	 * Just like setters...
	 */
	@Override
	public String getJavaGetterName(Definition definition, Mode mode)
	{
		return "get" + definition.getOutputName().substring(0, 1).toUpperCase() + definition.getOutputName().substring(1);
	}

	/**
	 * Override this method to define what a Java method generated from a database
	 * Definition should look like. This is used mostly for convenience methods
	 * when calling stored procedures and functions. This example shows how to
	 * set a prefix to a CamelCase version of your procedure
	 */
	@Override
	public String getJavaMethodName(Definition definition, Mode mode)
	{
		return "call" + org.jooq.tools.StringUtils.toCamelCase(definition.getOutputName());
	}

	/**
	 * Override this method to define how your Java classes and Java files should
	 * be named. This example applies no custom setting and uses CamelCase versions
	 * instead
	 */
	@Override
	public String getJavaClassName(Definition definition, Mode mode)
	{
		if (mode == Mode.RECORD)
			return definition.getOutputName().substring(0, 1).toUpperCase() + definition.getOutputName().substring(1) + "Record";
		else
			return definition.getOutputName().substring(0, 1).toUpperCase() + definition.getOutputName().substring(1);
	}

	/**
	 * Override this method to re-define the package names of your generated
	 * artefacts.
	 */
	@Override
	public String getJavaPackageName(Definition definition, Mode mode)
	{
		return super.getJavaPackageName(definition, mode);
	}

	/**
	 * Override this method to define how Java members should be named. This is
	 * used for POJOs and method arguments
	 */
	@Override
	public String getJavaMemberName(Definition definition, Mode mode)
	{
		return definition.getOutputName();
	}

	/**
	 * Override this method to define the base class for those artifacts that
	 * allow for custom base classes
	 */
	@Override
	public String getJavaClassExtends(Definition definition, Mode mode)
	{
		return Object.class.getName();
	}

	/**
	 * Override this method to define the interfaces to be implemented by those
	 * artefacts that allow for custom interface implementation
	 */
	@Override
	public List<String> getJavaClassImplements(Definition definition, Mode mode)
	{
		return Arrays.asList(Serializable.class.getName(), Cloneable.class.getName());
	}

	/**
	 * Override this method to define the suffix to apply to routines when
	 * they are overloaded.
	 * <p/>
	 * Use this to resolve compile-time conflicts in generated source code, in
	 * case you make heavy use of procedure overloading
	 */
	@Override
	public String getOverloadSuffix(Definition definition, Mode mode, String overloadIndex)
	{
		return "_OverloadIndex_" + overloadIndex;
	}
}