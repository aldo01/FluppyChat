//
//  ConfirmedRoomTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class ConfirmedRoomTableViewCell: BaseRoomTableViewCell {
    @IBOutlet weak var baseView: UIView!
    
    @IBOutlet weak var timeLabel: UILabel!
    @IBOutlet weak var messageLabel: UILabel!
    @IBOutlet weak var roomLabel: UILabel!
    @IBOutlet weak var incommingMessageCount: UITextField!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        rootView = baseView
    }

}
