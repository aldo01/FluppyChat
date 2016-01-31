//
//  SearchFriendTableViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/3/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class SearchFriendTableViewController: UITableViewController, SearchFriendDelegate {
    let NEW_ROOM_HEADER = "NEW_ROOM_"
    
    // the result list from parse
    var friendList : [ PFUser ] = [] {
        didSet {
            self.tableView.reloadData()
        }
    }
    let decoder = Decoder()
    var delegate : UpdateFriendListProtocol?
    var newRoomCreated : Bool = false

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationController?.navigationItem.title = "Search Friends"
    }

    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return friendList.count + 1
    }

    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        if indexPath.row == 0 {
            let cell = tableView.dequeueReusableCellWithIdentifier("SearchCell") as! SearchTableViewCell
            cell.delegate = self
            return cell
        } else {
            let cell = tableView.dequeueReusableCellWithIdentifier("UserCell") as! FriendTableViewCell
            let obj = friendList[indexPath.row - 1]
            cell.friendNameLabel.text = obj.username!
            PhotoContainer.getImageForUser(obj, imageView: cell.friendImage)
            return cell
        }
    }
    
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        print("Select row \(indexPath.row)")
        createRoom(friendList[indexPath.row - 1])
    }
    
    // this method calling for room creating
    private func createRoom( u : PFUser ) {
        newRoomCreated = true
        let meUser = PFUser.currentUser()! // get current user
        
        // create and save room
        let room = PFObject(className: "Room")
        room["Name"] = "\(meUser.username!), \(u.username!)"
        room["Creator"] = meUser
        room.saveInBackground()
        
        // sign me to room
        let obj1 = PFObject(className: "PeopleInRoom")
        obj1["people"] = meUser
        obj1["confirm"] = true
        obj1["room"] = room
        obj1.saveInBackgroundWithBlock { (res : Bool, err : NSError?) -> Void in
            if res && nil == err {
                self.navigationController!.popToRootViewControllerAnimated(true)
            } else {
                print("We have an problem res = \(res) err = \(err)")
            }
        }
        
        // sign user to room
        let obj2 = PFObject(className: "PeopleInRoom")
        obj2["people"] = u
        obj2["confirm"] = false
        obj2["room"] = room
        obj2.saveInBackground()
        
        // create and send push notification with invitation to room
        let push = PFPush()
        push.setChannel( NEW_ROOM_HEADER + u.objectId! )
        let data = [
            "Alert" : "You receive invite to new room",
            "AuthorName" : meUser.username!,
            "Status" : "INVITE",
            "aps" : ["alert" : ["body" : "You receive invite to new room"], "sound" : "default", "content-available" : 1]
        ]
        
        print("Send push to the chanel: \(NEW_ROOM_HEADER + u.objectId!) with data: \(data)")
        push.setData(data as [NSObject : AnyObject])
        push.sendPushInBackground()
    }
    
    // MARK: - Buttons action
    
    // action for searching friends
    func searchFrind(name: String) {
        let query = PFUser.query()
        query?.whereKey("username", equalTo: name)
        view.endEditing(true)
        query?.findObjectsInBackgroundWithBlock({ (list : [PFObject]?, err : NSError?) -> Void in
            if nil != list && nil == err {
                self.friendList = list as! [PFUser]
            } else {
                MessageAlert.showMessageForUser("An error ocurence")
            }
        })
  
        print("SEARCH FRIEND")
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        if newRoomCreated && nil != delegate {
            delegate?.updateFriendListWithCloud()
        }
    }
}
