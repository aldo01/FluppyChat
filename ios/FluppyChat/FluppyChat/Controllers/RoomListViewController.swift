//
//  RoomListViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit


class RoomListViewController: UITableViewController, UpdateRoomListProtocol, AcceptDeclineRoomProtocol,
UpdateFriendListProtocol{
    let OPEN_MESSAGE_SEGUE = "room2message"
    let OPEN_PROFILE_SEGUE = "roomController2ProfileController"
    let OPEN_SEARCH_SEGUE = "roomList2SearchFriends"
    let NEW_ROOM_HEADER = "NEW_ROOM_"
    
    static var this : RoomListViewController?
    
    // MARK: data fields
    
    var tableData : [PFObject] = [] // field that contain people in room object
    var otherPeopleInRoom : [PFObject] = []
    
    var roomData : [PFObject] = [] // array that contain all rooms
    override func viewDidLoad() {
        super.viewDidLoad()
        RoomListViewController.this = self
        obtainRoomList()
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "reloadTableView", name: UIApplicationDidBecomeActiveNotification, object: nil)
        
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "reloadTableView", name: UIApplicationWillEnterForegroundNotification, object: nil)
        
        // set navigation controller color
        self.navigationController?.navigationBar.barTintColor = ACTION_BAR_COLOR
        self.navigationController?.navigationBar.translucent = false
        self.navigationController?.navigationBar.tintColor = UIColor.whiteColor()
        self.navigationController?.navigationBar.titleTextAttributes = [ NSForegroundColorAttributeName : UIColor.whiteColor() ]
    }
    
    func reloadTableView() {
        tableView.reloadData()
    }
    
    // MARK: table view
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
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
            
            cell.showPeoples( room, friendList: otherPeopleInRoom )
            cell.roomLabel.text = room["Name"] as? String
            if nil != NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TEXT_KEY ) {
                cell.messageLabel?.text = NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TEXT_KEY ) as? String
            } else {
                cell.messageLabel.text = ""
            }
            
            if nil != NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TIME_KEY ) {
                let time = NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + TIME_KEY ) as! NSDate
                
                let formatter = NSDateFormatter()
                formatter.dateStyle = NSDateFormatterStyle.NoStyle
                formatter.timeStyle = .ShortStyle
                cell.timeLabel.text = formatter.stringFromDate(time)
            } else {
                cell.timeLabel.text = ""
            }
            
            if nil != NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + COUNT_KEY ) {
                cell.incommingMessageCount?.hidden = false
                print("Unread message count = \(NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + COUNT_KEY ))")
                cell.incommingMessageCount?.text = "\(NSUserDefaults.standardUserDefaults().objectForKey( room.objectId! + COUNT_KEY ) as! Int)"
            } else {
                cell.incommingMessageCount?.hidden = true
            }

            return cell
        } else {
            let cell = tableView.dequeueReusableCellWithIdentifier("NotConfirmedRoomCell") as! NotConfirmedTableViewCell
            
            cell.showPeoples( room, friendList: otherPeopleInRoom )
            cell.roomName.text = room["Name"] as? String
            cell.setDelegate(obj, delegate: self)
            
            return cell
        }
    }
    
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        print("Delete line: \(indexPath.row)")
        
        tableView.beginUpdates()
        let obj = tableData[indexPath.row]
        tableData.removeAtIndex(indexPath.row)
        obj.deleteInBackground()
        tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: UITableViewRowAnimation.Fade)
        tableView.endUpdates()
    }
    
    // click at some room
    override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        roomForOpen = tableData[indexPath.row]["room"] as! PFObject
        peoplesForOpening = []
        for obj in otherPeopleInRoom {
            if obj["room"].objectId! == roomForOpen.objectId! {
                peoplesForOpening.append(obj["people"] as! PFUser)
            }
        }
        
        self.performSegueWithIdentifier(OPEN_MESSAGE_SEGUE, sender: self)
    }

    func obtainFriendsList( roomData : [PFObject] ) {
        let query = PFQuery(className: "PeopleInRoom")
        query.whereKey("room", containedIn: roomData)
        query.includeKey("people")
        query.includeKey("room")
        do {
            otherPeopleInRoom = try query.findObjects()
        } catch _ {
            MessageAlert.errorAlert("Error ocqurence when obtain other user list")
        }
    }
    
    /**
    Get the list of room where is our user
    */
    func obtainRoomList() {
        SVProgressHUD.showWithStatus(LOADING_WORD)
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
                
                let newRoomKey = self.NEW_ROOM_HEADER + PFUser.currentUser()!.objectId!
                if !subscribedChannels.containsObject(newRoomKey) {
                    currentInstallation.addUniqueObject(newRoomKey, forKey: "channels")
                }

                do {
                    try currentInstallation.save()
                    print("Subscribe list: \(currentInstallation.channels!)")
                } catch (_) {
                    print("Error ocqurence when save installation")
                }
                self.roomData = rList
                self.tableData = uList
                
                self.obtainFriendsList( self.roomData )
                self.tableView.reloadData()
                SVProgressHUD.dismiss()
            }
        }
    }
    
    // MARK: click actions
    
    @IBAction func logoutAction(sender: AnyObject) {
        let currentInstallation = PFInstallation.currentInstallation()
        currentInstallation.channels = []
        currentInstallation.saveInBackground()
        
        PFUser.logOut()
        self.dismissViewControllerAnimated(true, completion: nil)
    }

    var roomForOpen : PFObject!
    var peoplesForOpening : [PFUser]!
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if nil == segue.identifier {
            return
        }
        
        // open messanger controller
        if OPEN_MESSAGE_SEGUE == segue.identifier! {
            let controller = segue.destinationViewController as! MessagingTableViewController

            controller.delegate = self
            controller.room = roomForOpen
        }
        
        // open profile controller
        if OPEN_PROFILE_SEGUE == segue.identifier! {
            let controller = segue.destinationViewController as! ProfileViewController
            controller.delegate = self
        }
        
        if OPEN_SEARCH_SEGUE == segue.identifier! {
            let controller = segue.destinationViewController as! SearchFriendTableViewController
            controller.delegate = self
        }
    }
    
    // MARK: update table view when if data changed in room
    
    func updateFriendsList(val : Bool) {
        print("updateFriendsList \(val)")
        if val {
            tableView.reloadData()
        }
    }
    
    // MARK: accept or decline room
    
    func acceptRoom( peopleInRoom : PFObject ) {
        peopleInRoom["confirm"] = true
        peopleInRoom.saveInBackground()
        tableView.reloadData()
    }
    
    func declineRoom( peopleInRoom : PFObject ) {
        peopleInRoom.deleteInBackground()
        
        let index = tableData.indexOf(peopleInRoom)
        tableData.removeAtIndex(index!)
        tableView.beginUpdates()
        let indexPath = NSIndexPath(forRow: index!, inSection: 0)
        tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: UITableViewRowAnimation.Fade)
        tableView.endUpdates()
    }
    
    // update room list when user back from search friend controller
    func updateFriendListWithCloud() {
        obtainRoomList()
    }
}
