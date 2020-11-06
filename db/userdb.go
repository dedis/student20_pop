package db

import (
	"crypto/sha1"
	"errors"
	"github.com/boltdb/bolt"
	"strconv"
	src "student20_pop/classes"
	"time"
)

const UserName = "user.db"


/*
 * opens the User DB. creates it if not exists.
 * don't forget to close the database afterwards
 */
func OpenUserDB() (*bolt.DB, error) {
	db, err := bolt.Open(UserName, 0600, nil)
	if err != nil {
		return nil, err
	}

	return db, nil
}

/**
 * Function will return an error if the DB was already initialized
 */
func InitUserDB(db *bolt.DB) error {
	err := db.Update(func(tx *bolt.Tx) error {
		b, err1 := tx.CreateBucket([]byte("ids"))
		if err1 != nil {
			return err1
		}
		err1 = b.Put([]byte("count"), []byte(strconv.Itoa(0)))
		return err1
	})
	return err
}

// TODO don't forget to increment count when adding a user


/**
 * Function to create a new user and store it in the DB
 * @returns : the id of the created user (+ event error)
 */
func CreateUser(id string) (error) {

	db, e := OpenUserDB()
	defer db.Close()
	if e != nil {
		return nil, e
	}

	err := db.Update(func(tx *bolt.Tx) error {

		bkt := tx.Bucket([]byte("ids"))
		if bkt == nil {
			return errors.New("bkt does not exist")
		}

		// instantiate a user with no subscribe nor publish rights
		err1 = b.Put([]byte(id), []byte(""))
		if err1 != nil {
			return err1
		}
		return nil
	})

	return err
}


/**
 * Check that the attestation of a user is correct
 */
func checkUserValidity(id []byte) bool {
	user, err := GetFromID(id)
	attestation := lao.Attestation

	//TODO do something??

	return computed == attestation
}


/**
* Retrieve value from a given ID key, and update it with a new subscribtion or publish rights
* returns error message
*/
func SubscribeUserDB (userId []byte, channelId []byte) error {

	//TODO correct the if checks
	// TODO create functions in jsonHelper addSubscribe, addPublish

	updatedString := addSubscribe(oldString, channelId)


	db, e := OpenUserDB()
	defer db.Close()
	if e != nil {
		return lao, e
	}
	
	err := db.Update(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("ids"))
		if b == nil {
			return errors.New("bkt does not exist")
		}

		err1 = b.Put(userid, updatedString)
		if err1 != nil {
			return err1
		}
		return nil
	})

	return err
}


/**
 * Returns a string which contains the subscribe and publish rights in the user database which matches the id passed an argument
 */
func GetUserDataFromID(userid []byte) ([]byte, error) {

	var data []byte

	db, e := OpenUserDB()
	defer db.Close()
	if e != nil {
		return lao, e
	}
	
	err := db.View(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("ids"))
		if b == nil {
			return errors.New("bkt does not exist")
		}

		data = b.Get(userid)
		return nil
	})

	return data, err
}


//TODO move those functions to json helper but we might never need them
/*
func GetSubscribeOfUserFromId {
	data, err = GetUserDataFromID(userid)
	//slice json
}

func GetPublishOfUserFromId
*/
