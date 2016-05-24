package com.luvbrite.utils;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import com.luvbrite.web.models.Sequence;

public class SequenceGen {

	public SequenceGen() {}

	public synchronized static long getNextSequence(Datastore datastore, String _id){
		
		long sequence = 0l;
		
		try {

			Query<Sequence> seq = datastore.createQuery(Sequence.class).field("_id").equal(_id);
			UpdateOperations <Sequence> updateSeq = datastore.createUpdateOperations(Sequence.class).inc("seq", 1);
			
			datastore.update(seq, updateSeq, true);
			
			Sequence result = datastore.createQuery(Sequence.class).field("_id").equal(_id).get();
			sequence = result.getSeq();
			
			//System.out.println("--- Seq " + _id + " - # " + sequence);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return sequence;		
		
	}

	public synchronized static boolean setSequence(Datastore datastore, String _id, long sequence){
		
		boolean success = true;
		
		try {
			
			Query<Sequence> seq = datastore.createQuery(Sequence.class).field("_id").equal(_id);
			UpdateOperations <Sequence> updateSeq = datastore.createUpdateOperations(Sequence.class).set("seq", sequence);
			

			datastore.update(seq, updateSeq, true);		

		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return success;		
		
	}
}
