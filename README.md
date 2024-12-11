# OCR Text-to-speech

> Android application for reading text in images outloud utilizing Google ML Kit and Microsoft Azure
<hr>

In this project, we developed an Android application using Kotlin and Jetpack Compose. The application informs the user when there is a text on camera which allows user to take a picture, select a text, and have it read aloud.

## Application Preview
![OCR Text-to-speech](https://github.com/user-attachments/assets/0ba2cc87-3730-4ebe-ad8c-92fa6891aff4)

## Feature
* **Detect Text in Image**
  * The text detected can be in English, Cantonese, or Mandarin
  * After detecting a text, user can take a picture
* **Analyze Text in Image**
  * Identify regions of text in the captured image
  * Buffer of previous image which includes text to guarantee the image captured contains text
  * Option to choose a region of text and read it out loud using text-to-speech
* **Image History**
  * History of images captured containing text, sorted by time and date
  * The image is saved as JPEG and bitmap format for fast retrieval
* **Setting Page**
  * Configuration to change language, online or offline version, and voice speed
 
## Project Contents
This project has followed Android's architecture guidelines where the application is divided into UI & Data, and UI is further divided using View Model. Here are the folders:
* **base** -> contains configurations for text-to-speech
* **history** -> contains configurations for the storing and retrieving image history
* **ocr** -> contains configurations for detecting text in image
* **type** -> contains all data class and objects
* **ui** -> contains UI related components, further divided into:
  * *screens* -> contains all screen UI layouts
  * *theme* -> contains the theme configuration for the application such as fonts and colour palette
  * *viewmodels* -> contains the logic for each individual screen
* **util** -> contains helper and other utility functions

## Contributor
This is a collaboration project between @Lipencomm1, @hardy733, and me. If you want to edit this project, you can clone it to your own GitHub repository and edit it there. Thanks!
