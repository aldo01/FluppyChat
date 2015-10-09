//
//  FriendTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/3/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class FriendTableViewCell: UITableViewCell {
    @IBOutlet weak var friendImage: UIImageView!
    @IBOutlet weak var friendNameLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
