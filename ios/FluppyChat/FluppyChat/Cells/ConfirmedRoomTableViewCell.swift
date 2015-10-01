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
    var pictureCount = 0
    
    @IBOutlet weak var baseView: UIView!

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
        var listForShow : [PFUser] = []
        for obj in ConfirmedRoomTableViewCell.otherPeopleInRoom {
            if obj["room"].objectId! == room.objectId! {
                listForShow.append(obj["people"] as! PFUser)
            }
        }
        
        for u in listForShow {
            addPicture(u)
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
        image.frame = CGRect( x: LEFT_MARGING + IMAGE_SIZE * pictureCount, y: 0, width: IMAGE_SIZE, height: IMAGE_SIZE )
        image.layer.cornerRadius = image.frame.size.width / 2
        image.clipsToBounds = true
        image.contentMode = UIViewContentMode.ScaleAspectFill
        
        self.baseView.addSubview(image)
        pictureCount++
    }
}
