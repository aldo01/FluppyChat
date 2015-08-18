//
//  ConfirmedRoomTableViewCell.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 17.08.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

import UIKit

class ConfirmedRoomTableViewCell: UITableViewCell {
    var cellCount : Int = 0
    let CELL_SIZE = 64
    var imgArray : [UIImageView]!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func addUserImage( img : UIImage ) {
        let imgView : UIImageView = UIImageView()
        
        imgView.image = img
        imgView.frame = CGRect( x: CELL_SIZE * cellCount, y: 0, width: CELL_SIZE, height: CELL_SIZE )
        self.viewForBaselineLayout()!.addSubview(imgView)
        //imgArray.append(imgView)
        cellCount++
    }
}
