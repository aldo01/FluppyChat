//
//  RoomListViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class RoomListViewController: UITableViewController {
    let OPEN_MESSAGE_SEGUE = "room2message"
    
    // MARK: data fields
    
    var tableData : [PFObject] = [] { // field that contain people in room object
        didSet {
            RoomCellTableViewCell.obtainFriendsList( roomData )
            self.tableView.reloadData()
        }
    }
    
    var roomData : [PFObject] = [] // array that contain all rooms

    override func viewDidLoad() {
        super.viewDidLoad()

        obtainRoomList()
    }
    
    // MARK: table view
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        print( "Room count = \(tableData.count)" )
        return tableData.count
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let obj = tableData[indexPath.row]
        if nil == obj["room"] {
            return UITableViewCell()
        }
        let room = obj["room"] as! PFObject
        
        if ( obj["confirm"] as! Bool ) {
            let cell = tableView.dequeueReusableCellWithIdentifier("ConfirmedRoomCell") as! ConfirmedRoomTableViewCell
            
            cell.showPeoples( room )
            return cell
        }
        
        return UITableViewCell()
    }

    
    /**
    Get the list of room where is our user
    */
    private func obtainRoomList() {
        let query = PFQuery(className: "PeopleInRoom")
        query.whereKey("people", equalTo: PFUser.currentUser()! )
        query.includeKey("people")
        query.includeKey("room")
        query.findObjectsInBackgroundWithBlock { ( list : [PFObject]?, err : NSError?) -> Void in
            if ( nil == err && nil != list ) {
                var rList : [PFObject] = []
                var uList : [PFObject] = []
                
                for obj in list! {
                    if nil != obj["room"] {
                        uList.append( obj )
                        rList.append( obj["room"] as! PFObject )
                    }
                }

                self.roomData = rList
                self.tableData = uList
            }
        }
    }
    
    // click at some room
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        roomForOpen = tableData[indexPath.row]["room"] as! PFObject
        peoplesForOpening = []
        for obj in ConfirmedRoomTableViewCell.otherPeopleInRoom {
            if obj["room"].objectId! == roomForOpen.objectId! {
                peoplesForOpening.append(obj["people"] as! PFUser)
            }
        }
        
        self.performSegueWithIdentifier(OPEN_MESSAGE_SEGUE, sender: self)
    }
    
    // MARK: click actions
    
    @IBAction func logoutAction(sender: AnyObject) {
        PFUser.logOut()
        self.dismissViewControllerAnimated(true, completion: nil)
    }

    var roomForOpen : PFObject!
    var peoplesForOpening : [PFUser]!
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if nil == segue.identifier {
            return
        }
        
        if OPEN_MESSAGE_SEGUE == segue.identifier! {
            let controller = segue.destinationViewController as! MessagingTableViewController

            print("push: \(roomForOpen)")
            controller.userHere = peoplesForOpening
            controller.room = roomForOpen
        }
    }
    
    
}
