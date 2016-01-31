//
//  PhotoContainer.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 9/23/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//
//  This class contain the user images.
//  Called when app need some photo.
//  If photo there is in archive - return,
//  else download him
//

class PhotoContainer {
    static var photosDic : [ PFUser : UIImage ]!
    
    static func getImageForUser( user : PFUser, imageView : UIImageView ) {
        if ( nil != PhotoContainer.photosDic[user] ) {
            imageView.image = PhotoContainer.photosDic[user]!
            setLayoutParamsForImage(imageView)
        } else {
            downloadPhotoInBackground(user, imageView : imageView)
        }
    }
    
    static func getImageForUser( userId : String, imageView : UIImageView ) {
        for (user, photo) in photosDic {
            if userId == user.objectId! {
                imageView.image = photo
                setLayoutParamsForImage(imageView)
                return
            }
        }
        
        let query = PFUser.query()
        query?.whereKey("objectId", equalTo: userId)
        query?.findObjectsInBackgroundWithBlock({ (objects : [PFObject]?, err : NSError?) -> Void in
            if nil == err && 0 != objects?.count {
                downloadPhotoInBackground(objects![0] as! PFUser, imageView : imageView)
            }
        })
    }
    
    static private func setLayoutParamsForImage( image : UIImageView ) {
        image.layer.cornerRadius = image.frame.size.width / 2
        image.clipsToBounds = true
        image.contentMode = UIViewContentMode.ScaleAspectFill
    }
    
    static func downloadPhotoInBackground(user : PFUser, imageView : UIImageView) {
        // download photo in background
        if nil != user["profilepic"] {
            let file = user["profilepic"] as! PFFile
            file.getDataInBackgroundWithBlock({ ( image : NSData?, err : NSError?) -> Void in
                if ( nil == err ) {
                    let image = UIImage(data: image!)
                    
                    // add image to the dictionary
                    PhotoContainer.photosDic[user] = image
                    
                    imageView.image = image
                    setLayoutParamsForImage(imageView)
                }
            })
        } else {
            // if user don't have photo use standart
            let image = UIImage(named: "user_photo")
            
            // add image to the dictionary
            PhotoContainer.photosDic[user] = image
            
            imageView.image = image
            setLayoutParamsForImage(imageView)
        }
    }
    
    static func replacePhotoForUser( user : PFUser, image : UIImage ) {
        photosDic[user] = image
    }
}
