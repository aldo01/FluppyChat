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

let TIME_KEY = "_TIME"
let TEXT_KEY = "_TEXT"
let COUNT_KEY = "_COUNT"

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
    var messageList : [PFObject] = []
    
    // utils
    var decoder = Decoder()
    var delegate : UpdateRoomListProtocol!
    
    // data
    var keyboardIsShowen  = false
    var newMessage = false // get new message during controller work
    
    // ui
    @IBOutlet weak var oldTableView: UITableView!
    
    
    static var messageController : MessagingTableViewController?
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        MessagingTableViewController.messageController = self
        
        tableView.estimatedRowHeight = 100.0
        tableView.rowHeight = UITableViewAutomaticDimension
        
        // clear unread message
        if nil != NSUserDefaults.standardUserDefaults().objectForKey(room.objectId! + COUNT_KEY) {
            NSUserDefaults.standardUserDefaults().setValue(nil, forKey: room.objectId! + COUNT_KEY)
            NSUserDefaults.standardUserDefaults().synchronize()
            newMessage = true
            print("Value not is nil")
        }
        print("Value is nil")
    }
    
    override class func tableViewStyleForCoder(decoder: NSCoder) -> UITableViewStyle {
        return UITableViewStyle.Plain;
    }
    
    // MARK: - Table view data source
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return messageList.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = oldTableView.dequeueReusableCellWithIdentifier("MessageCell") as! MessageTableViewCell
        let message = messageList[indexPath.row]
        
        if 0 == indexPath.row {
            let messageText = decoder.decode( (message["Text"] as? String)! )
            
            if nil != NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TEXT_KEY ) {
                newMessage = true
            }
            
            // save last message
            NSUserDefaults.standardUserDefaults().setValue("" == messageText ? " " : messageText, forKey: room.objectId! + TEXT_KEY)
            NSUserDefaults.standardUserDefaults().setValue(nil == message.createdAt ? NSDate() : message.createdAt, forKey: room.objectId! + TIME_KEY)
            NSUserDefaults.standardUserDefaults().synchronize()
        }
        
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
        cell.dateLabel.text = nil == message.createdAt ? formatter.stringFromDate( NSDate() ) : formatter.stringFromDate(message.createdAt!)
        
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
        newMessage = true
        
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
        var data : [ String : AnyObject ] = ["Alert" : encryptMessage]
        data["Author"] = PFUser.currentUser()!.objectId!
        data["AuthorName"] = PFUser.currentUser()!.username!
        data["AndroidId"] = deviceId
        data["RoomId"] = room.objectId!
        let username = PFUser.currentUser()!.username!
        data["aps"] = ["alert" : ["body" : "\(username): \(messageText)"], "sound" : "default", "content-available" : 1]
        print("Data for sending: \(data)")
        push.setData(data)
        push.sendPushInBackground()
        
        showNewMessage(obj)
        self.textView.text = ""
    }
    
    static func receiveMessage( data : JSON ) -> Bool {
        if nil == messageController {
            return false
        }
        
        if ((messageController?.deviceId)!) == data["AndroidId"].stringValue {
            print("Is equal")
            return true
        }

        print("Is not equal")
        print("1" + (messageController?.deviceId)! + "1")
        print("1" + data["AndroidId"].stringValue + "1")
        print("show message")
        let messageObject = PFObject(className: "Message")
        messageObject["State"] = INCOMMING_MESSAGE
        messageObject["UserName"] = data["AuthorName"].stringValue
        messageObject["Text"] = data["Alert"].stringValue
        messageObject["UserId"] = data["Author"].stringValue
        
        messageController?.showNewMessage(messageObject)
        return true
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        
        MessagingTableViewController.messageController = nil
        if nil != delegate {
            delegate.updateFriendsList(newMessage)
        }
    }
}

extension String {
    func isEqualToString(find: String) -> Bool {
        return String(format: self) == find
    }
}
