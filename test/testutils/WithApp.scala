package testutils

import scala.reflect.{ClassTag, _}

import play.api.Application
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.inject.{BindingKey, Injector}

/**
  * Provides a fresh Play application, an injector and some useful methods. If you wish to provide additional bindings/
  * configuration, override the corresponding fields
  */
trait WithApp {

  /**
    * The bindings meant to override the ones provided in the modules files. Override this if you wish to provide some
    */
  def overrideModules: Seq[GuiceableModule] = Seq()

  /**
    * Additional (or overriding) configuration. Same as those in application.conf.
    * Override this if you wish to provide some
    */
  def conf: Seq[(String, Any)] = Seq()

  final lazy val app: Application =
    GuiceApplicationBuilder().overrides(overrideModules: _*).configure(conf: _*).build()

  implicit def implicitApp: Application = app

  final lazy val injector: Injector = app.injector

  /**
    * Shorthand for "injector.instanceOf[U]"
    * @tparam U The type of the element to inject
    * @return The injected element
    */
  def inject[U: ClassTag]: U = injector.instanceOf[U]

  /**
    * Inject an element qualified with some name
    * @param name The name of the qualified element
    * @tparam U The type of the element to inject
    * @return The injected element
    */
  def injectNamed[U: ClassTag](name: String): U =
    injector.instanceOf(BindingKey(classTag[U].runtimeClass.asInstanceOf[Class[U]]).qualifiedWith(name))
}
