/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.homesampleapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.time.Duration
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test to verify e2e commissioning of a Matter device in the Home sample app.
 *
 * Before running this test, make sure that 1) The Matter device is running and advertising on the
 * same network as the app. The SETUP_CODE
 *
 * ```
 *    below should match with the set up code of the Matter device.
 * ```
 * 2) You only have one Home structure in the Google home app.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class AddDeviceTest {

  private val TEN_SECONDS = Duration.ofSeconds(10).toMillis()
  private val ONE_MINUTE = Duration.ofSeconds(60).toMillis()
  private val TWO_MINUTES = Duration.ofSeconds(120).toMillis()
  private val SCAN_QR_CODE_TITLE = By.text("Scan the QR code")
  private val TRY_WITH_SETUP_CODE_BUTTON = By.text("Set up without QR code")
  private val ENTER_SETUP_CODE_TITLE = By.textContains("Enter Matter pairing code")
  private val SETUP_CODE_TEXTBOX = UiSelector().className("android.widget.EditText").instance(0)

  /** The SETUP_CODE should match the one used for the Matter device */
  private val SETUP_CODE = "34970112332"
  private val NEXT_BUTTON = By.text("Next")
  private val DEVICE_CONNECTED = By.text("Device connected")
  private val CONNECT_ACCOUNT_TITLE = By.text("Connect .* your Google Account".toPattern())
  private val AGREE_BUTTON = By.text("I Agree")
  private val DONE_BUTTON = By.text("Done")
  private val DEVICE_NAME = "Test light"
  private val DEVICE_NAME_TEXTBOX = UiSelector().className("android.widget.EditText").instance(0)
  private lateinit var device: UiDevice

  @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)
  @get:Rule var hiltRule = HiltAndroidRule(this)

  @Before
  fun init() {
    // Initialize UiDevice instance
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    // Hilt injection.
    hiltRule.inject()
  }

  fun triggerScanForQRCode() {
    // Verify Add Device button is displayed.
    onView(withId(R.id.addDeviceButton)).check(matches(isDisplayed()))
    // Click the Add Device button.
    onView(withId(R.id.addDeviceButton)).perform(click())
    // Verify the Google play services screen is displayed
    // to scan the QR code.
    //fixme assertNotNull(device.wait(Until.hasObject(SCAN_QR_CODE_TITLE), TEN_SECONDS))
    assertNotNull(device.wait(Until.hasObject(TRY_WITH_SETUP_CODE_BUTTON), TEN_SECONDS))
    // Click on Try with setup code option.
    device.findObject(TRY_WITH_SETUP_CODE_BUTTON).click()
  }

  fun enterSetupCode() {
    // Verify the enter setup code screen.
    assertNotNull(device.wait(Until.hasObject(ENTER_SETUP_CODE_TITLE), TEN_SECONDS))
    // Enter the setup code.
    device.findObject(SETUP_CODE_TEXTBOX).setText(SETUP_CODE)
    // Click the next button
    val nextButton: UiObject2 = device.findObject(NEXT_BUTTON)
    assertNotNull(nextButton.wait(Until.clickable(true), TEN_SECONDS))
    nextButton.click()
  }

  fun clickDone() {
    // Verify the connect to your Google Account screen.
    assertNotNull(device.wait(Until.hasObject(DEVICE_CONNECTED), TEN_SECONDS))
    // Click on "Done"
    device.wait(Until.findObject(DONE_BUTTON), ONE_MINUTE).click()
  }

  fun completeSetUp() {
    // Verify that the device is successfully commissioned and is ready.
    assertNotNull(device.wait(Until.hasObject(DONE_BUTTON), TWO_MINUTES))
    // Enter the device name.
    device.findObject(DEVICE_NAME_TEXTBOX).setText(DEVICE_NAME)
    // Click done.
    device.wait(Until.findObject(DONE_BUTTON), TEN_SECONDS).click()
    // Verify the light is present on Matterhorn
    assertNotNull(device.wait(Until.hasObject(By.text(DEVICE_NAME)), TEN_SECONDS))
  }

  @Test
  fun addDevice() {
    triggerScanForQRCode()
    enterSetupCode()
    clickDone()
    // fixme -->
    // enter device name...
    // control device...
    completeSetUp()
    // Prevent abrupt shutdown of app
    Thread.sleep(5000)
  }
}
