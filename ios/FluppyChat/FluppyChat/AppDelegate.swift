//
//  AppDelegate.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/22/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
        print("didFinishLaunchingWithOptions")
        
        // init parse application
        Parse.setApplicationId("fYiaMJQcSGKjQB3AwhpGmpFoBvE8UiLJAAQMGKjh", clientKey: "t5lfmPcRZjfRHnBGlPYS984ahstd1nHriMdirpA9")
        
        PhotoContainer.photosDic = [:]
        
        let type : UIUserNotificationType = [.Alert, .Badge, .Sound]
        
        let setting = UIUserNotificationSettings(forTypes: type, categories: nil)
        
        application.registerUserNotificationSettings(setting)
        application.registerForRemoteNotifications()
        
        let pushedData = launchOptions?.indexForKey(UIApplicationLaunchOptionsRemoteNotificationKey)
        print("Push data = \(pushedData)")
        
        return true
    }

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    }
    
    func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
        
    }
    
    func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject]) {
        print("push received")
        print(userInfo)
        
        let data = JSON(userInfo)
        if MessagingTableViewController.receiveMessage( data ) {
            return
        }
        
        saveData(data)
    }
    
    func application(application: UIApplication,
        didReceiveRemoteNotification userInfo: [NSObject : AnyObject],
        fetchCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
     
        let data = JSON(userInfo)
        if MessagingTableViewController.receiveMessage( data ) {
            return
        }
            
        saveData(data)
        completionHandler(UIBackgroundFetchResult.NewData)
    }
    
    private func saveData( data : JSON ) {
        print("push saved")
        let countMessageKey = data["RoomId"].stringValue + COUNT_KEY
        let countIncommingMessage = NSUserDefaults.standardUserDefaults().valueForKey(countMessageKey) as? Int ?? 0
        
        NSUserDefaults.standardUserDefaults().setValue(data["aps"]["alert"]["body"].stringValue, forKey: data["RoomId"].stringValue + TEXT_KEY)
        NSUserDefaults.standardUserDefaults().setValue(NSDate(), forKey: data["RoomId"].stringValue + TIME_KEY)
        NSUserDefaults.standardUserDefaults().setValue(Int(countIncommingMessage + 1), forKey: countMessageKey)
        NSUserDefaults.standardUserDefaults().synchronize()
        
        print("Application state = \(UIApplication.sharedApplication().applicationState)")
        if UIApplicationState.Active == UIApplication.sharedApplication().applicationState {
            if nil != RoomListViewController.this {
                RoomListViewController.this?.reloadTableView()
            }
        }
    }
}

