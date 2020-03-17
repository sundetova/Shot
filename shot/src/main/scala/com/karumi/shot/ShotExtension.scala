package com.karumi.shot

import scala.beans.BeanProperty

object ShotExtension {
  val name = "shot"
}

class ShotExtension(@BeanProperty var appId: String,
                    @BeanProperty var instrumentationTestTask: String,
                    @BeanProperty var runInstrumentation: Boolean,
                    @BeanProperty var referenceDir: String) {

  def this() = this(null, null, true, null)

  def getOptionAppId: Option[String] = Option(getAppId)

  def getOptionInstrumentationTestTask: Option[String] =
    Option(getInstrumentationTestTask)

  def getOptionReferenceDir: Option[String] =
        Option(getReferenceDir)
}
