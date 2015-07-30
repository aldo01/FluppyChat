//
//  notConfirmedRoomTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 30.07.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit

class NotConfirmedRoomTableViewCell: UITableViewCell {
    @IBOutlet weak var titleTV: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    @IBAction func acceptAction(sender: AnyObject) {
    }
    
    @IBAction func declineAction(sender: AnyObject) {
    }

}
