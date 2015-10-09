//
//  SearchFriendTableViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/3/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class SearchFriendTableViewController: UITableViewController, SearchFriendDelegate {
    var friendList : [ PFUser ] = [] {
        didSet {
            self.tableView.reloadData()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()

    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Table view data source

    override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }

    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
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
    
    func searchFrind(name: String) {
/*
        let query = PFUser.query()
        query!.whereKey("username", equalTo: name)
        query!.findObjectsInBackgroundWithBlock({ (list : [PFUser]?, err : NSError?) -> Void in
            if nil == err && nil != list {
                friendList = list
            }
        })
  */
        print("SEARCH FRIEND")
    }
}
