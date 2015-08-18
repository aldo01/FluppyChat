//
//  LeftRoomViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 17.08.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit
import Parse

class LeftRoomViewController: UITableViewController {
        var selectedMenuItem : Int = 0
        override func viewDidLoad() {
            super.viewDidLoad()
            
            // Customize apperance of table view
            tableView.contentInset = UIEdgeInsetsMake(64.0, 0, 0, 0) //
            tableView.separatorStyle = .None
            tableView.backgroundColor = UIColor.clearColor()
            tableView.scrollsToTop = false
            
            // Preserve selection between presentations
            self.clearsSelectionOnViewWillAppear = false
            
            tableView.selectRowAtIndexPath(NSIndexPath(forRow: selectedMenuItem, inSection: 0), animated: false, scrollPosition: .Middle)
        }
        
        override func didReceiveMemoryWarning() {
            super.didReceiveMemoryWarning()
            // Dispose of any resources that can be recreated.
        }
        
        // MARK: - Table view data source
        
        override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
            // Return the number of sections.
            return 1
        }
        
        override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
            // Return the number of rows in the section.
            return 1
        }
        
        override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
            
            var cell = tableView.dequeueReusableCellWithIdentifier("CELL") as? UITableViewCell
            
            if (cell == nil) {
                cell = UITableViewCell(style: UITableViewCellStyle.Default, reuseIdentifier: "CELL")

                let imgQuery : PFFile = PFUser.currentUser()!["profilepic"] as! PFFile
                println("Load image")
                imgQuery.getDataInBackgroundWithBlock({ (imageData: NSData?, error: NSError?) -> Void in
                    println("Image loaded")
                    if ( nil == error ) {
                        let parentView = cell!.viewForBaselineLayout()!
                        let image = UIImage(data:imageData!)
                        let imageView = UIImageView(image: image!)
                        imageView.frame = parentView.frame
                        imageView.contentMode = UIViewContentMode.ScaleAspectFit
                        parentView.addSubview(imageView)
                    } else {
                        println( "Error: \(error)" )
                    }
                })
            }
            
            return cell!
        }
        
        override func tableView(tableView: UITableView, heightForRowAtIndexPath indexPath: NSIndexPath) -> CGFloat {
            return 200.0
        }

}
