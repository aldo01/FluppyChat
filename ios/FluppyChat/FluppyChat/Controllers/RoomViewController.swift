//
//  RoomViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 30.07.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit
import Parse

class RoomViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    @IBOutlet weak var tableView: UITableView!
    
    var roomList : [PFObject] = []{
        didSet {
            tableView.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        // download list of room
        let query = PFQuery(className: "PeopleInRoom")
        query.whereKey("people", equalTo: PFUser.currentUser()!)
        query.includeKey("room")
        query.findObjectsInBackgroundWithBlock { (list : [AnyObject]?, err : NSError?) -> Void in
            if ( nil == err ) {
                var rList : [PFObject] = []
                for o in list! {
                    rList.append((o as? PFObject)!)
                }
                
                println( "Room count: \(rList.count)" )
                self.roomList = rList
            } else {
                println(err)
            }
        }
    }

    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        println( "Room count: \(roomList.count)" )
        return roomList.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell : UITableViewCell!
        let people : PFObject = roomList[indexPath.row]
        println( "Room: \(people)" )
        if ( people["confirm"] as! Bool ) {
            var cell : UITableViewCell! = tableView.dequeueReusableCellWithIdentifier("confirmedRoomCell") as! UITableViewCell
            let room : PFObject = (people["room"] as? PFObject)!
            cell.textLabel!.text = room["Name"] as? String
            return cell
        } else {
            var cell : NotConfirmedRoomTableViewCell! = tableView.dequeueReusableCellWithIdentifier("notConfirmedCell") as! NotConfirmedRoomTableViewCell
            let room : PFObject = (people["room"] as? PFObject)!
            cell.titleTV.text = room["Name"] as? String
            return cell
        }
    }
    
    var selectedRoom : PFObject!
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        //CODE TO BE RUN ON CELL TOUCH
        selectedRoom = (roomList[indexPath.row]["room"] as? PFObject)!
        self.performSegueWithIdentifier("showMessage", sender: self)
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject!) {
        
        // Create a new variable to store the instance of PlayerTableViewController
        let destinationVC = segue.destinationViewController as! MessagingViewController
        destinationVC.room = selectedRoom
    }

    
}
