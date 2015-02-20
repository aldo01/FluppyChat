//
//  FrindListViewController.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 21.02.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit

class FrindListViewController: UIViewController, UITableViewDataSource {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func tableView(tableView: UITableView,
        numberOfRowsInSection section: Int) -> Int {
        return 3;
    }
    
    func tableView(tableView: UITableView,
        cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        let cell : FriendTableCell = tableView.dequeueReusableCellWithIdentifier("friendCell") as FriendTableCell
        cell.friendName.text = "123"
            
        return cell

    }
    
    
    @IBAction func searchFrendsCell() {
        
    }
}
