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

class MessagingTableViewController: SLKTextViewController {
    // constants
    let KEYBOARD_SIZE : CGFloat = 236
    let ROOM_HEADER = "ROOM_"
    static let INCOMMING_MESSAGE = "incomming_message"
    let deviceId = UIDevice.currentDevice().identifierForVendor!.UUIDString
    
    // data for showing
    var room : PFObject! {
        didSet {
           obtainMessageHistory()
        }
    }
    var userHere : [PFObject]!
    var messageList : [PFObject] = []
    
    // utils
    var decoder = Decoder()
    
    // data
    var keyboardIsShowen  = false
    
    // ui
    @IBOutlet weak var oldTableView: UITableView!
    
    
    static var messageController : MessagingTableViewController?
    

    override func viewDidLoad() {
        super.viewDidLoad()
        
        MessagingTableViewController.messageController = self
  
        tableView.estimatedRowHeight = 100.0
        tableView.rowHeight = UITableViewAutomaticDimension
        
    }
    
    override class func tableViewStyleForCoder(decoder: NSCoder) -> UITableViewStyle {
        return UITableViewStyle.Plain;
    }

    // MARK: - Table view data source

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        print("message count: \(messageList.count)")
        return messageList.count
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = oldTableView.dequeueReusableCellWithIdentifier("MessageCell") as! MessageTableViewCell
        let message = messageList[indexPath.row]
        print("Get message for row: \(indexPath.row)")
        
        return getIncommingDefault(message, cell: cell)
    }
    
    
    /**
     * return cell for object from push
     */
    func getIncommingDefault( message : PFObject, cell : MessageTableViewCell ) -> MessageTableViewCell {
        // create date fromat
        let formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterStyle.MediumStyle
        formatter.timeStyle = .ShortStyle
        cell.dateLabel.text = formatter.stringFromDate( NSDate() )

        let messageText = decoder.decode( (message["Text"] as? String)! )
        cell.messageLabel.text = "" == messageText ? " " : messageText

        if nil != message["State"] {
            PhotoContainer.getImageForUser(message["UserId"] as! String, imageView: cell.userImage)
            cell.userName.text = message["UserName"] as? String
        } else {
            PhotoContainer.getImageForUser(message["User"] as! PFUser, imageView: cell.userImage)
            cell.userName.text = (message["User"] as! PFUser).username!
        }
        cell.transform = tableView.transform
        
        return cell
    }
    
    /**
    Ask in parse message history for current room
    */
    private func obtainMessageHistory() {
        let query = PFQuery(className: "Message")
        query.whereKey("Room", equalTo: room)
        query.orderByDescending("createdAt")
        query.includeKey("User")
        query.limit = 50
        query.findObjectsInBackgroundWithBlock { (list : [PFObject]?, err : NSError?) -> Void in
            if nil == err && nil != list {
                // all ok!
                self.messageList = list!
                self.tableView.reloadData()
            } else {
                print(err)
                print(list)
            }
        }
    }
    
    private func showNewMessage(messageObject : PFObject) {
        let indexPath = NSIndexPath(forRow: 0, inSection: 0)
        let rowAnimation = UITableViewRowAnimation.Top
        let scrollPostion = UITableViewScrollPosition.Top
        
        tableView.beginUpdates()
        messageList.insert(messageObject, atIndex: 0)
        tableView.insertRowsAtIndexPaths([indexPath], withRowAnimation: rowAnimation)
        tableView.endUpdates()
        
        tableView.scrollToRowAtIndexPath(indexPath, atScrollPosition: scrollPostion, animated: true)
    }
    
    /**
    Send message to the other user
    */
    override func didPressRightButton(sender: AnyObject!) {
        let messageText = self.textView.text!
        
        if messageText.isEmpty {
            MessageAlert.showMessageForUser("Message is blank")
            return
        }
        
        // encrypt message
        let encryptMessage = decoder.encode( messageText )
        
        // save message in parse
        let obj = PFObject(className: "Message")
        obj["User"] = PFUser.currentUser()
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
        data["AndroidId"] = deviceId
        push.setData(data)
        push.sendPushInBackground()
        
        showNewMessage(obj)
        self.textView.text = ""
    }
    
    static func receiveMessage( data : JSON ) -> Bool {
        if nil == messageController {
            return false
        }

        if ((messageController?.deviceId)!).isEqualToString(data["AndroidID"].stringValue) {
            return false
        }
        
        print(UIDevice.currentDevice().identifierForVendor!.UUIDString)
        print(data["AndroidId"].stringValue)
        print("show message")
        let messageObject = PFObject(className: "Message")
        messageObject["State"] = INCOMMING_MESSAGE
        messageObject["UserName"] = data["AuthorName"].stringValue
        messageObject["Text"] = data["Alert"].stringValue
        messageObject["UserId"] = data["Author"].stringValue
        
        messageController?.showNewMessage(messageObject)
        return true
    }
}

extension String {
    func isEqualToString(find: String) -> Bool {
        return String(format: self) == find
    }
}
