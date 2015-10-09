//
//  SearchTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/3/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class SearchTableViewCell: UITableViewCell {
    @IBOutlet weak var userNameField: UITextField!
    var delegate : SearchFriendDelegate!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    @IBAction func searchAction(sender: AnyObject) {
        let username = userNameField.text!
        if "" == username {
            MessageAlert.showMessageForUser("The field is empty")
        } else {
            if nil != delegate {
                delegate.searchFrind(username)
            }
        }
    }

}
