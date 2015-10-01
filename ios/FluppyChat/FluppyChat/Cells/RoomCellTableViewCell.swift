//
//  RoomCellTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/1/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class RoomCellTableViewCell: UITableViewCell {
    static var otherPeopleInRoom : [PFObject] = []
    
    static func obtainFriendsList( roomData : [PFObject] ) {
        let query = PFQuery(className: "PeopleInRoom")
        query.whereKey("room", containedIn: roomData)
        query.includeKey("people")
        query.includeKey("room")
        do {
        otherPeopleInRoom = try query.findObjects()
        } catch _ {
            MessageAlert.errorAlert("Error ocqurence when obtain other user list")
        }
    }

}
