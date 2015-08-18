//
//  MessagingViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 30.07.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit
import Parse

class MessagingViewController: UIViewController, UITableViewDataSource {
    // constants
    let PUSH_PREFIX = "ROOM_"
    
    // ui elements
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var messageView: UITextField!
    
    var room : PFObject! {
        didSet {
            obtainMessageHistory()
        }
    }
    var userImages : NSMutableDictionary!
    var listUser : [PFObject] = []
    var listMessage : [PFObject] = [] {
        didSet {
            tableView.reloadData()
            tableView.setContentOffset( CGPoint(x: 0, y: listMessage.count * 88 - Int(tableView.frame.height)), animated: true)
        }
    }
    
    var decryptor : Decrypt!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        decryptor = Decrypt()
    }

    private func obtainListOfUser() {
        let query = PFQuery(className: "PeopleInRoom")
        query.whereKey("room", equalTo: room)
        query.includeKey("people")
        query.findObjectsInBackgroundWithBlock { ( list : [AnyObject]?, err : NSError?) -> Void in
            if ( nil == err ) {
                var lUser : [PFUser] = []
                
                for o in list! {
                    lUser.append( (o["people"] as? PFUser)! )
                }
                
                self.listUser = lUser
            }
        }
    }
    
    private func obtainMessageHistory() {
        println( "Room \(room)" )
        let query = PFQuery(className: "Message")
        query.whereKey("Room", equalTo: room)
        query.includeKey("User")
        query.orderByAscending("createdAt")
        println(query)
        query.findObjectsInBackgroundWithBlock { ( list : [AnyObject]?, err : NSError?) -> Void in
            if ( nil == err ) {
                var lMessage : [PFObject] = []
                
                for o in list! {
                    lMessage.append( (o as? PFObject)! )
                }
                
                println( "Message count: \(list!.count)" )
                self.listMessage = lMessage
            } else {
                println( err )
            }
        }
    }

    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return listMessage.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let message : PFObject = listMessage[indexPath.row]
        if ( PFUser.currentUser()?.objectId == (message["User"] as? PFUser)!.objectId  ) {
            let cell : MyMessageTableViewCell = (tableView.dequeueReusableCellWithIdentifier("MyMessage") as? MyMessageTableViewCell)!
            cell.messageTextTV!.text = decryptor.decrypt((message["Text"] as? String)!)
            let username = (message["User"] as? PFUser)?.username!
            cell.authorTextTV!.text = username!
            var img : AnyObject? = userImages[ (message["User"] as? PFUser)!.objectId! ]
            if nil != img {
                cell.userImage.image = img as? UIImage
            }
            
            return cell
        } else {
            let cell : FriendMessageTableViewCell = (tableView.dequeueReusableCellWithIdentifier("friendMessage") as? FriendMessageTableViewCell)!
            cell.textField!.text = decryptor.decrypt((message["Text"] as? String)!)
            cell.authorTextView!.text = (message["User"] as? PFUser)?.username!
            var img : AnyObject? = userImages[ (message["User"] as? PFUser)!.objectId! ]
            if nil != img {
                cell.userImage.image = img as? UIImage
            }
            
            return cell
        }
    }

    @IBAction func sendAction(sender: AnyObject) {
        if ( messageView.text.isEmpty ) {
            UIAlertView(title: "Message", message: "Message is empty", delegate: nil, cancelButtonTitle: "Ok").show()
        }
        
        let messageText = decryptor.encrypt( messageView.text ) // encrypt message from text view
        
        // Send a notification
        let push = PFPush()
        push.setChannel( PUSH_PREFIX + room.objectId! )
        let data = [
            "Alert" : messageText,
            "Author" : PFUser.currentUser()!.objectId!,
            "AuthorName" : PFUser.currentUser()!.username!
        ]
        push.setData(data)
        push.sendPushInBackgroundWithBlock({ ( res : Bool, err : NSError?) -> Void in
            if ( nil != err ) {
                println("An error ocqurence when register device \(err)")
            } else {
                println("Message sended")
            }
        })
        
        // save message in cloud
        let message = PFObject(className : "Message")
        message["Room"] = room
        message["Text"] = messageText
        message["User"] = PFUser.currentUser()
        message.saveInBackgroundWithBlock {
            (success: Bool, error: NSError?) -> Void in
            if (success) {
                println("Message saved")
            } else {
                println("An error ocqurence when save the method \(error)")
            }
        }
        
        messageView.text = "" // clear text view
    }
}
