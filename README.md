# Wrist Route 

**Wrist Route** is an Android application that enables real-time **turn-by-turn navigation** on smartwatches via notifications. It’s designed for users whose smartwatches **lack built-in GPS navigation**, providing seamless guidance without the need to constantly check their phones.  

![Wrist Route](https://github.com/user-attachments/assets/7dfc94ff-8018-47a0-acd5-6e825c6b37f5)  

---  

## 🌟 Features  
✅ **Turn-by-turn navigation updates** sent directly to smartwatches  
✅ **Concise notifications** every 50 meters  
✅ Works on **smartwatches without built-in navigation**  
✅ **Hands-free experience** for joggers, cyclists, and commuters  

---  

## 🔧 How It Works  
🔹 Uses **Mapbox Navigation SDK** to extract real-time navigation data  
🔹 Sends structured notifications in the format:  
   ```less
   [Remaining Distance] | [Next Maneuver] | [Current Maneuver] | [Remaining Duration]
   ```  
🔹 **Runs in the background** to provide uninterrupted navigation  

---  

## 🛠 Tech Stack  
- **Kotlin (Android Native)**  
- **Mapbox Navigation SDK**  
- **Notification Manager** for real-time alerts  

---  

## 🔥 What’s Next?  
🚀 **Route Recalculation** when a user deviates  
🔋 **Battery Optimization** for longer usage  
🌍 **Offline Support** for navigation without an internet connection  

---  

## 🎯 Get Started  
Clone the repository and start exploring:  
```bash
git clone https://github.com/yourusername/WristRoute.git
cd WristRoute
```
Run the project in **Android Studio** and start navigating smarter! 🚀  

### Create a Secret Token  
A secret access token is required to download the SDK dependencies in your Android project. This token is used by Gradle to authenticate with the Mapbox Maven server where the SDK packages are hosted.  

#### To create a secret token, follow these steps:  
1. Go to your [account's tokens page](https://account.mapbox.com/access-tokens/).  
2. Click the **Create a token** button.  
3. Name your token (e.g., `InstallTokenAndroid`).  
4. Scroll down to the **Secret Scope** section and check the `Downloads:Read` scope box.  
5. Click the **Create token** button at the bottom of the page.  
6. Enter your password to confirm token creation.  
7. Copy your token and save it securely (you will only have one chance to do this).  

#### Configure your Secret Token  
Add your secret token to your global `gradle.properties` file. The global `gradle.properties` file is located in your Gradle user home folder.  

If you don't have a `gradle.properties` file, create one and add the following line:  
```properties
MAPBOX_DOWNLOADS_TOKEN=YOUR_SECRET_MAPBOX_ACCESS_TOKEN
```

---  

### Configure Your Public Token  
Your app must have a public access token configured to associate its usage of Mapbox resources with your account.  

#### Follow these steps to add a public access token:  
1. Open your project folder or create a new project in **Android Studio**.  
2. If creating a new project, we recommend using the **Empty Activity** project type.  
3. Locate the **resource folder**:  
   - In the project explorer, open `app/res/values`.  
4. Create a **new resource file**:  
   - Right-click on the `values` folder  
   - Select **New > Values Resource File**  
   - Name the file `mapbox_access_token.xml`  
   - Click **OK**.  
5. In the new file, copy and paste the following code:  
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources xmlns:tools="http://schemas.android.com/tools">
       <string name="mapbox_access_token" translatable="false" tools:ignore="UnusedResources">YOUR_MAPBOX_ACCESS_TOKEN</string>
   </resources>
   ```  
   - Replace `YOUR_MAPBOX_ACCESS_TOKEN` with a token from your [account's tokens page](https://account.mapbox.com/access-tokens/).  

---   

## 💡 Contributions Welcome!  
Feel free to **open issues, suggest improvements, or fork the repo** to build upon this project.  

📩 Have feedback or ideas? Let’s discuss in the **Issues** section!  

---
