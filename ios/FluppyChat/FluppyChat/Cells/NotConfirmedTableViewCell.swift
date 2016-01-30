//
//  NotConfirmedTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 1/30/16.
//  Copyright © 2016 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class NotConfirmedTableViewCell: BaseRoomTableViewCell {
    @IBOutlet weak var roomName: UILabel!
    @IBOutlet weak var baseView: UIView!
    
    var delegate : AcceptDeclineRoomProtocol?
    var room : PFObject?

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        rootView = baseView
    }
    
    func setDelegate( room : PFObject, delegate : AcceptDeclineRoomProtocol ) {
        self.delegate = delegate
        self.room = room
    }
    
    @IBAction func declineAction(sender: AnyObject) {
        if nil != delegate {
            delegate!.declineRoom(room!)
        }
    }
    
    @IBAction func acceptAction(sender: AnyObject) {
        if nil != delegate {
            delegate!.acceptRoom(room!)
        }
    }
}
