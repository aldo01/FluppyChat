//
//  RoomViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 30.07.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit
import Parse

class RoomViewController: UIViewController, ENSideMenuDelegate, UITableViewDataSource, UITableViewDelegate {
    @IBOutlet weak var tableView: UITableView!
    let ROOM_PREFIX = "ROOM_"
    let userImages : NSMutableDictionary = [ "123" : UIImage() ]
    
    var roomList : [PFObject] = []{
        didSet {
            tableView.reloadData()
            
            /*
            let currentInstallation = PFInstallation.currentInstallation()
            let subscribedChannels = (currentInstallation.channels as? [String])!
            for r in self.roomList {
                let roomId = ROOM_PREFIX + r.objectId!
                var res : Bool = true
                for c in subscribedChannels {
                    if c == roomId {
                        res = false
                    }
                }
                
                if ( res ) {
                    println( "Add \(roomId)" )
                    currentInstallation.addUniqueObject(roomId, forKey: "channels")
                    currentInstallation.saveInBackgroundWithBlock { ( res : Bool, err : NSError?) -> Void in
                        if ( nil != err ) {
                            println("An error ocqurence when register device \(err)")
                        }
                    }
                } else {
                    println( "Exist \(roomId)" )
                }
            }
*/
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.hidesBackButton = true
        
        self.sideMenuController()?.sideMenu?.delegate = self
        obtainRoomList()
    }
    
    private func obtainRoomList() {
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

                self.roomList = rList
            } else {
                println(err)
            }
        }
    }

    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return roomList.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell : UITableViewCell!
        let people : PFObject = roomList[indexPath.row]
        println( "Room: \(people)" )
        if ( people["confirm"] as! Bool ) {
            var cell : ConfirmedRoomTableViewCell! = tableView.dequeueReusableCellWithIdentifier("confirmedCell") as! ConfirmedRoomTableViewCell
            let room : PFObject = (people["room"] as? PFObject)!
            
            // get all people from this room
            let query = PFQuery(className: "PeopleInRoom")
            query.whereKey("room", equalTo: room)
            query.includeKey("people")
            query.findObjectsInBackgroundWithBlock { ( list : [AnyObject]?, err : NSError?) -> Void in
                if ( nil == err ) {
                    var lUser : [PFUser] = []
                    
                    for o in list! {
                        let u : PFUser = (o["people"] as? PFUser)!
                        if nil == self.userImages[u.objectId!] {
                            
                            // load image
                            let imgQuery : PFFile? = u["profilepic"] as? PFFile
                            if nil != imgQuery {
                            imgQuery!.getDataInBackgroundWithBlock({ (imageData: NSData?, error: NSError?) -> Void in
                                println("Image loaded")
                                if ( nil == error ) {
                                    self.userImages[u.objectId!] = UIImage(data: imageData!)
                                    self.showSmallImage(u, cell: cell)
                                } else {
                                    println( "Error: \(error)" )
                                }
                            })
                            }
                        } else {
                            self.showSmallImage(u, cell: cell)
                        }
                    }
                }
            }
            
            return cell
        } else {
            var cell : NotConfirmedRoomTableViewCell! = tableView.dequeueReusableCellWithIdentifier("notConfirmedCell") as! NotConfirmedRoomTableViewCell
            let room : PFObject = (people["room"] as? PFObject)!
            cell.titleTV.text = room["Name"] as? String
            return cell
        }
    }
    
    private func showSmallImage( u : PFUser, cell : ConfirmedRoomTableViewCell ) {
        // add image to view
        if var data : UIImage = self.userImages[u.objectId!] as? UIImage {
            cell.addUserImage(data)
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
        destinationVC.userImages = self.userImages
    }
    
    @IBAction func logoutAction(sender: AnyObject) {
        self.navigationController?.popViewControllerAnimated(true)
    }
}
