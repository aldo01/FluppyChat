//
//  RoomListViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class RoomListViewController: UITableViewController, UpdateRoomListProtocol {
    let OPEN_MESSAGE_SEGUE = "room2message"
    let NEW_ROOM_HEADER = "NEW_ROOM_"
    
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

        let data = NSUserDefaults.standardUserDefaults().valueForKey("123")
        print("Value for key 123: \(data)")
        obtainRoomList()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "reloadTableView", name: UIApplicationDidBecomeActiveNotification, object: nil)
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "reloadTableView", name: UIApplicationWillEnterForegroundNotification, object: nil)
    }
    
    func reloadTableView() {
        tableView.reloadData()
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
            
            NSUserDefaults.standardUserDefaults()
            
            cell.showPeoples( room )
            cell.roomLabel.text = room["Name"] as?String
            if nil != NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TEXT_KEY ) {
                cell.messageLabel?.text = NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TEXT_KEY ) as? String
            }
            
            if nil != NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TIME_KEY ) {
                let time = NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TIME_KEY ) as! NSDate
                
                let formatter = NSDateFormatter()
                formatter.dateStyle = NSDateFormatterStyle.NoStyle
                formatter.timeStyle = .ShortStyle
                cell.timeLabel.text = formatter.stringFromDate(time)
            }

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
                
                var subscribedChannels = []
                let currentInstallation = PFInstallation.currentInstallation()
                if nil != currentInstallation.channels {
                    subscribedChannels = (currentInstallation.channels!) as NSArray
                } else {
                    subscribedChannels = NSArray()
                }
                
                print("Subscribe list: \(subscribedChannels)")
                for obj in list! {
                    if nil != obj["room"] {
                        uList.append( obj )
                        let room = obj["room"] as! PFObject
                        rList.append( room )
                        
                        // subscribe to th chanel
                        let chanelId = "ROOM_\(room.objectId!)"
                        if !subscribedChannels.containsObject(chanelId)  {
                            currentInstallation.addUniqueObject(chanelId, forKey: "channels")
                            print("Subscribe to the chanel: \(chanelId)")
                        }
                    }
                }
                
                if !subscribedChannels.containsObject(self.NEW_ROOM_HEADER + PFUser.currentUser()!.objectId!) {
                    currentInstallation.addUniqueObject(self.NEW_ROOM_HEADER, forKey: "channels")
                }

                do {
                    try currentInstallation.save()
                    print("Subscribe list: \(currentInstallation.channels!)")
                } catch (_) {
                    print("Error ocqurence when save installation")
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
            controller.delegate = self
            controller.room = roomForOpen
        }
    }
    
    func updateFriendsList(val : Bool) {
        print("updateFriendsList \(val)")
        if val {
            tableView.reloadData()
        }
    }
}
