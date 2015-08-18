//
//  MyMessageTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 01.08.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit

class MyMessageTableViewCell: UITableViewCell {
    @IBOutlet weak var messageTextTV: UILabel!
    @IBOutlet weak var authorTextTV: UILabel!
    @IBOutlet weak var userImage: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
}
