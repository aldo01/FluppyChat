//
//  BaseRoomTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 1/30/16.
//  Copyright © 2016 Dmytro Bohachevskyy. All rights reserved.
//

import UIKit

class BaseRoomTableViewCell: UITableViewCell {
    let IMAGE_SIZE = 50
    let LEFT_MARGING = 20
    let IMAGE_MARGIN = 10
    let Y_POSITION = 7
    var pictureCount = 0
    var peopleShowed = false
    
    var pictures : [UIImageView] = []
    var rootView : UIView?
    
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
    func showPeoples( room : PFObject, friendList : [PFObject] ) {
        if peopleShowed {
            redrawPictures()
            return;
        }
        
        peopleShowed = true
        for obj in friendList {
            if obj["room"].objectId! == room.objectId! {
                let u = obj["people"] as! PFUser
                if u.objectId! != PFUser.currentUser()?.objectId! {
                    createPicture(u)
                }
            }
            
            if 2 <= pictureCount {
                break
            }
        }
    }
    
    private func redrawPictures() {
        pictureCount = 0
        for imageView in pictures {
            addPictureToTheView(imageView)
            
            if 2 <= pictureCount {
                break
            }
        }
    }
    
    /**
     Add picture to the layout
     */
    func createPicture( user : PFUser ) {
        let image = UIImageView()
        
        // set image resource
        PhotoContainer.getImageForUser(user, imageView: image)
        pictures.append(image)
        addPictureToTheView(image)
    }
    
    private func addPictureToTheView( image : UIImageView ) {
        // set layout params
        image.frame = CGRect( x: LEFT_MARGING + (IMAGE_MARGIN) * pictureCount, y: Y_POSITION, width: IMAGE_SIZE, height: IMAGE_SIZE )
        
        if nil != rootView {
            self.rootView!.addSubview(image)
        }
        pictureCount++
    }

}
