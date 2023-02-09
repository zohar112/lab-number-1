#ifndef WORKOUT_H_
#define WORKOUT_H_

#include <string>

enum WorkoutType{
    ANAEROBIC, MIXED, CARDIO
};

class Workout{
public:
    Workout(int w_id, std::string w_name, int w_price, WorkoutType w_type);
    int getId() const;
    std::string getName() const;
    int getPrice() const;
    WorkoutType getType() const;
    std::string toString();
private:
	const int id;
    const std::string name;
    const int price;
    const WorkoutType type;
};


#endif