//
//  Decoder.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyy on 10/1/15.
//  Copyright © 2015 Dmytro Bohachevskyy. All rights reserved.
//

class Decoder {
    let PASSWORD = "password"
    
    func decode(text : String) -> String {
        return AESCrypt.decrypt(text, password: PASSWORD)
    }
    
    func encode(text : String) -> String {
        return AESCrypt.encrypt(text, password: PASSWORD)
    }

}
