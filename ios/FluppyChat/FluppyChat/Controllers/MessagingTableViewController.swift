//
//  MessagingTableViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/1/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class MessagingTableViewController: UITableViewController {
    // data for showing
    var room : PFObject! {
        didSet {
            obtainMessageHistory()
        }
    }
    var userHere : [PFObject]!
    var messageList : [PFObject] = [] {
        didSet {
            self.tableView.reloadData()
        }
    }
    
    // utils
    var decoder = Decoder()

    override func viewDidLoad() {
        super.viewDidLoad()

        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        self.navigationItem.rightBarButtonItem = self.editButtonItem()
        
        tableView.estimatedRowHeight = 44.0
        tableView.rowHeight = UITableViewAutomaticDimension
    }

    // MARK: - Table view data source

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        print("message count: \(messageList.count)")
        return messageList.count
    }

    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("MessageCell") as! MessageTableViewCell
        
        let message = messageList[indexPath.row]
        let user = message["User"] as! PFUser
        
        cell.userName.text = user.username!
        cell.dateLabel.text = "\(message.createdAt!)"
        cell.messageLabel.text = decoder.decode( (message["Text"] as? String)! )
        PhotoContainer.getImageForUser(user, imageView: cell.userImage)

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
            } else {
                print(err)
                print(list)
            }
        }
    }
    

    /*
    // Override to support conditional editing of the table view.
    override func tableView(tableView: UITableView, canEditRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        if editingStyle == .Delete {
            // Delete the row from the data source
            tableView.deleteRowsAtIndexPaths([indexPath], withRowAnimation: .Fade)
        } else if editingStyle == .Insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(tableView: UITableView, moveRowAtIndexPath fromIndexPath: NSIndexPath, toIndexPath: NSIndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(tableView: UITableView, canMoveRowAtIndexPath indexPath: NSIndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
