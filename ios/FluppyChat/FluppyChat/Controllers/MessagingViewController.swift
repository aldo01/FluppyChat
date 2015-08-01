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
    @IBOutlet weak var tableView: UITableView!
    
    var room : PFObject! {
        didSet {
            obtainMessageHistory()
        }
    }
    var listUser : [PFObject] = []
    var listMessage : [PFObject] = [] {
        didSet {
            tableView.reloadData()
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
            let cell : FriendMessageTableViewCell = (tableView.dequeueReusableCellWithIdentifier("friendMessage") as? FriendMessageTableViewCell)!
            cell.textLabel!.text = decryptor.decrypt((message["Text"] as? String)!)
            let username = (message["User"] as? PFUser)?.username!
            println( "Username: \(username!)" )
            cell.authorTextView!.text = username!
            return cell
        } else {
            let cell : FriendMessageTableViewCell = (tableView.dequeueReusableCellWithIdentifier("friendMessage") as? FriendMessageTableViewCell)!
            cell.textLabel!.text = decryptor.decrypt((message["Text"] as? String)!)
            cell.authorTextView!.text = (message["User"] as? PFUser)?.username!
            return cell
        }
    }

}
