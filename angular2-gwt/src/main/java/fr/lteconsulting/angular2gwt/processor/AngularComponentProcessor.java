package fr.lteconsulting.angular2gwt.processor;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes( { AngularComponentProcessor.DirectiveAnnotationFqn, AngularComponentProcessor.ComponentAnnotationFqn, AngularComponentProcessor.InjectableAnnotationFqn, AngularComponentProcessor.NgModuleAnnotationFqn } )
@SupportedSourceVersion( SourceVersion.RELEASE_8 )
public class AngularComponentProcessor extends AbstractProcessor
{
	public final static String DirectiveAnnotationFqn = "fr.lteconsulting.angular2gwt.Directive";
	public final static String ComponentAnnotationFqn = "fr.lteconsulting.angular2gwt.Component";
	public final static String InjectableAnnotationFqn = "fr.lteconsulting.angular2gwt.Injectable";
	public final static String NgModuleAnnotationFqn = "fr.lteconsulting.angular2gwt.NgModule";

	public final static String DIRECTIVE_HELPER_CLASS_SUFFIX = "_AngularDirective";
	public final static String COMPONENT_HELPER_CLASS_SUFFIX = "_AngularComponent";
	public final static String INJECTABLE_HELPER_CLASS_SUFFIX = "_AngularInjectable";
	public final static String NG_MODULE_HELPER_CLASS_SUFFIX = "_AngularModule";

	@Override
	public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		//JSNIOutputProcessor processor = new JSNIOutputProcessor( processingEnv );		
		JsInteropOutputProcessor processor = new JsInteropOutputProcessor( processingEnv );
		
		return processor.process( annotations, roundEnv );
	}
}
