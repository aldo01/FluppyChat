//
//  MessagingTableViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/1/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//
//  This view controller allow user to see all messages and send new
//

import UIKit

class MessagingTableViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    // constants
    let KEYBOARD_SIZE : CGFloat = 236
    let ROOM_HEADER = "ROOM_"
    
    // data for showing
    var room : PFObject! {
        didSet {
            obtainMessageHistory()
        }
    }
    var userHere : [PFObject]!
    var messageList : [PFObject] = [] {
        didSet {
            self.tableView.reloadData()
            self.showBottomTable()
        }
    }
    
    // utils
    var decoder = Decoder()
    
    // ui
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var messageTextField: UITextField!
    
    // data
    var keyboardIsShowen  = false
    

    override func viewDidLoad() {
        super.viewDidLoad()
        
        tableView.estimatedRowHeight = 44.0
        tableView.rowHeight = UITableViewAutomaticDimension
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardWillShow:", name: UIKeyboardWillShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "keyboardWillHide:", name: UIKeyboardWillHideNotification, object: nil)
    }

    // MARK: - Table view data source

    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        print("message count: \(messageList.count)")
        return messageList.count
    }

    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("MessageCell") as! MessageTableViewCell
        
        let message = messageList[indexPath.row]
        let user = message["User"] as! PFUser
        
        cell.userName.text = user.username!
        
        // create date fromat
        let formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterStyle.MediumStyle
        formatter.timeStyle = .ShortStyle
        if (message.createdAt != nil) {
            cell.dateLabel.text = formatter.stringFromDate(message.createdAt!)
        } else {
            // show current time and date
            cell.dateLabel.text = formatter.stringFromDate(NSDate())
        }
        cell.messageLabel.text = decoder.decode( (message["Text"] as? String)! )
        PhotoContainer.getImageForUser(user, imageView: cell.userImage)

        return cell
    }
    
    // hide keyboard
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        if keyboardIsShowen {
            messageTextField.endEditing(true)
        }
    }
    
    /**
    Ask in parse message history for current room
    */
    private func obtainMessageHistory() {
        let query = PFQuery(className: "Message")
        query.whereKey("Room", equalTo: room)
        query.orderByAscending("createdAt")
        query.includeKey("User")
        query.limit = 50
        query.findObjectsInBackgroundWithBlock { (list : [PFObject]?, err : NSError?) -> Void in
            if nil == err && nil != list {
                // all ok!
                self.messageList = list!
            } else {
                print(err)
                print(list)
            }
        }
    }
    
    /**
    Send message to the other user
    */
    @IBAction func sendAction(sender: AnyObject) {
        let messageText = messageTextField.text!
        
        if messageText.isEmpty {
            MessageAlert.showMessageForUser("Message is blank")
            return
        }
        
        // encrypt message
        let encryptMessage = decoder.encode( messageText )
        
        // save message in parse
        let obj = PFObject(className: "Message")
        obj["User"] = PFUser.currentUser()!
        obj["Room"] = room
        obj["Text"] = encryptMessage
        obj.saveInBackgroundWithBlock { (saved : Bool, err : NSError?) -> Void in
            if nil != err || !saved {
                MessageAlert.showMessageForUser("Message is not sent")
            }
        }
        
        // send push notification
        let push = PFPush()
        push.setChannel( ROOM_HEADER + room.objectId! )
        // create data for message
        var data : [ String : String ] = ["Alert" : encryptMessage]
        data["Author"] = PFUser.currentUser()!.objectId!
        data["AuthorName"] = PFUser.currentUser()!.username!
        data["AndroidId"] = UIDevice.currentDevice().identifierForVendor!.UUIDString
        push.setData(data)
        push.sendPushInBackground()
        
        messageList.append(obj)
        showBottomTable()
        messageTextField.text = ""
    }
    
    /**
    Up all screen when keyboard is appear
    */
    func keyboardWillShow(notification: NSNotification) {
        keyboardIsShowen = true
        // set default keyboard size
        var size : CGFloat = KEYBOARD_SIZE
        // get keyboard size
        if let userInfo = notification.userInfo {
            if let keyboardSize = (userInfo[UIKeyboardFrameBeginUserInfoKey] as? NSValue)?.CGRectValue() {
                size = keyboardSize.height
            }
        }
        
        self.view.frame.origin.y -= size
    }
    
    /**
    Down all screen when keyboard is disappear
    */
    func keyboardWillHide(notification: NSNotification) {
        keyboardIsShowen = false
                // set default keyboard size
        var size : CGFloat = KEYBOARD_SIZE
         // get keyboard size
        if let userInfo = notification.userInfo {
            if let keyboardSize = (userInfo[UIKeyboardFrameBeginUserInfoKey] as? NSValue)?.CGRectValue() {
                size = keyboardSize.height
            }
        }
        
        self.view.frame.origin.y += size
    }
    
    // scroll table view to the bottom
    // show last messages
    private func showBottomTable() {
        if tableView.contentSize.height > tableView.bounds.size.height {
            let offset = CGPointMake(0, tableView.contentSize.height - tableView.bounds.size.height)
            tableView.setContentOffset(offset, animated: false)
        }
    }
}
