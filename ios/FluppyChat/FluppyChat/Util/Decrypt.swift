//
//  Decrypt.swift
//  FluppyChat
//
//  Created by Dmytro Bohachevskyi on 31.07.15.
//  Copyright (c) 2015 Dmytro Bohachevskyi. All rights reserved.
//

class Decrypt {
    private let PASSWORD = "password"

    func decrypt( text : String ) -> String {
        return AESCrypt.decrypt(text, password: PASSWORD)
    }
    
}
