//
//  ConfirmedRoomTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class ConfirmedRoomTableViewCell: RoomCellTableViewCell {
    let IMAGE_SIZE = 50
    let LEFT_MARGING = 20
    let IMAGE_MARGIN = 10
    let Y_POSITION = 7
    var pictureCount = 0
    var peopleShowed = false
    
    @IBOutlet weak var baseView: UIView!
    var pictures : [UIImageView] = []
    
    @IBOutlet weak var timeLabel: UILabel!
    @IBOutlet weak var messageLabel: UILabel!
    @IBOutlet weak var roomLabel: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    /**
    Show the list of images
    */
    func showPeoples( room : PFObject ) {
        if peopleShowed {
            return;
        }
        
        peopleShowed = true
        roomLabel.text = room["Name"] as?String
        for obj in ConfirmedRoomTableViewCell.otherPeopleInRoom {
            if obj["room"].objectId! == room.objectId! {
                let u = obj["people"] as! PFUser
                if u.objectId! != PFUser.currentUser()?.objectId! {
                    addPicture(u)
                }
            }
            
            if 2 <= pictureCount {
                break
            }
        }
    }
    
    /**
    Add picture to the layout
    */
    func addPicture( user : PFUser ) {
        let image = UIImageView()
        
        // set image resource
        PhotoContainer.getImageForUser(user, imageView: image)
        
        // set layout params
        image.frame = CGRect( x: LEFT_MARGING + (IMAGE_MARGIN) * pictureCount, y: Y_POSITION, width: IMAGE_SIZE, height: IMAGE_SIZE )
        
        self.baseView.addSubview(image)
        pictureCount++
        pictures.append(image)
    }
}
