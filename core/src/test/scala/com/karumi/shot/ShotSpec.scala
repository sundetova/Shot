package com.karumi.shot

import com.karumi.shot.android.Adb
import com.karumi.shot.domain.Config
import com.karumi.shot.domain.model.AppId
import com.karumi.shot.mothers.AppIdMother
import com.karumi.shot.reports.{ConsoleReporter, ExecutionReporter}
import com.karumi.shot.screenshots.{
  ScreenshotsComparator,
  ScreenshotsDiffGenerator,
  ScreenshotsSaver
}
import com.karumi.shot.ui.Console
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

object ShotSpec {
  private val appIdConfigError =
    "🤔  Error found executing screenshot tests. The appId param is not configured properly. You should configure the appId following the plugin instructions you can find at https://github.com/karumi/shot"
}

class ShotSpec
    extends FlatSpec
    with Matchers
    with BeforeAndAfter
    with MockFactory
    with Resources {

  import ShotSpec._

  private var shot: Shot = _
  private val adb = mock[Adb]
  private val files = mock[Files]
  private val console = mock[Console]
  private val screenshotsComparator = mock[ScreenshotsComparator]
  private val screenshotsDiffGenerator = mock[ScreenshotsDiffGenerator]
  private val screenshotsSaver = mock[ScreenshotsSaver]
  private val reporter = mock[ExecutionReporter]
  private val consoleReporter = mock[ConsoleReporter]

  before {
    shot = new Shot(adb,
                    files,
                    screenshotsComparator,
                    screenshotsDiffGenerator,
                    screenshotsSaver,
                    console,
                    reporter,
                    consoleReporter)
  }

  "Shot" should "should delegate screenshots cleaning to Adb" in {
    val appId: Option[AppId] = AppIdMother.anyAppId
    val device: String = "emulator-5554"
    (adb.devices _).expects().returns(List(device))

    (adb.clearScreenshots _).expects(device, appId.get)

    shot.removeScreenshots(appId)
  }

  it should "pull the screenshots using the project metadata folder and the app id if" in {
    val appId = AppIdMother.anyAppId
    val device = "emulator-5554"
    val projectFolder = ProjectFolderMother.anyProjectFolder
    val expectedScreenshotsFolder = projectFolder + Config.screenshotsFolderName
    val expectedOriginalMetadataFile = projectFolder + Config.metadataFileName
    val expectedRenamedFile = projectFolder + Config.metadataFileName + "_" + device
    (adb.devices _).expects().returns(List(device))

    (console.show _).expects(*)
    (adb.pullScreenshots _)
      .expects(device, expectedScreenshotsFolder, appId.get)
    (files.rename _).expects(expectedOriginalMetadataFile, expectedRenamedFile)

    shot.downloadScreenshots(projectFolder, appId, device)
  }

  it should "configure adb path" in {
    val anyAdbPath = "/Library/androidsdk/bin/adb"

    shot.configureAdbPath(anyAdbPath)

    Adb.adbBinaryPath shouldBe anyAdbPath
  }

  it should "show an error if the app ID is not properly configured when cleaning screenshots" in {
    val appId = AppIdMother.anyInvalidAppId

    (console.showError _).expects(appIdConfigError)

    shot.removeScreenshots(appId)
  }

  it should "show an error if the app ID is not properly configured when pulling screenshots" in {
    val appId = AppIdMother.anyInvalidAppId
    val projectFolder = ProjectFolderMother.anyProjectFolder

    (console.showError _).expects(appIdConfigError)

    shot.downloadScreenshots(projectFolder, appId, "")
  }
}
