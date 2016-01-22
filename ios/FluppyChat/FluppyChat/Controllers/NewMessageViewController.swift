//
//  NewMessageViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 1/21/16.
//  Copyright © 2016 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class NewMessageViewController: SLKTextViewController {
    @IBOutlet weak var oldTableView: UITableView!
    
    
    override class func tableViewStyleForCoder(decoder: NSCoder) -> UITableViewStyle {
        return UITableViewStyle.Plain;
    }
    
    override func viewDidLoad() {
        
        // In progress in branch 'swift-example'
        super.viewDidLoad()
        
        self.tableView.rowHeight = 100.0
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 10
    }
    
    override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = oldTableView.dequeueReusableCellWithIdentifier("MessageCell") as! MessageTableViewCell
        cell.messageLabel?.text = "Some Text"
        cell.userName?.text = "Dmytro"
        
        
        cell.transform = self.tableView.transform
        
        return cell
    }
    
   
}
