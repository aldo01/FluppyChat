//
//  RoomListViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class RoomListViewController: UITableViewController {
    /* DATA FIELDS */
    var tableData : [PFObject] = [] { // field that contain people in room object
        didSet {
            RoomCellTableViewCell.obtainFriendsList( roomData )
            self.tableView.reloadData()
        }
    }
    
    var roomData : [PFObject] = [] // array that contain all rooms
    var otherUsers : [PFUser] = [] // array that contain other users
        

    /* DATA FIELDS */

    override func viewDidLoad() {
        super.viewDidLoad()

        obtainRoomList()
    }
    
    internal override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        print( "Room count = \(tableData.count)" )
        return tableData.count
    }
    

    internal override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
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

}
