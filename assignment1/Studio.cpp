
#include "../include/Studio.h"
#include <vector>
#include <string>
#include "fstream"
#include "sstream"

//------------------RULE OF 5------------------//
//constructor
Studio:: Studio() : open(false), trainers(), workout_options(), actionsLog(), customerId(0){}

//move constructor
Studio::Studio(Studio &&other): open(other.open), trainers(std::move(other.trainers)),
                                                           workout_options(std::move(other.workout_options)),
                                                           actionsLog(std::move(other.actionsLog)), customerId(other.customerId){}
//copy constructor
Studio::Studio(const Studio &other): open(other.open), trainers(), workout_options(other.workout_options),actionsLog(), customerId(other.customerId){
    for(unsigned int i=0; i<other.trainers.size(); i++) {
        trainers.push_back(other.trainers[i]->clone());
    }
    for(unsigned int k=0; k<other.actionsLog.size(); k++) {
        actionsLog.push_back(other.actionsLog[k]->clone());
    }
}
//assigment operator
Studio &Studio::operator=(const Studio &other) {
    if(this != &other){
        open = other.open;
        this->clear();
        for(const auto &work: other.workout_options){
            workout_options.push_back(work);
        }
        for(int i=0; i<other.getNumOfTrainers(); i++) {
            trainers.push_back(new Trainer(*other.trainers[i]));
        }
        for(unsigned int j=0; j<other.actionsLog.size(); j++) {
                actionsLog.push_back(other.actionsLog[j]->clone());
        }
        customerId = other.customerId;
    }
    return *this;
}
//move assgiment operator
Studio &Studio::operator=(Studio &&other) {
    open = other.open;
    trainers= std::move(other.trainers);
    actionsLog = std::move(other.actionsLog);
    workout_options=std::move(other.workout_options);
    customerId = other.customerId;
    return *this;
}
//destructor
Studio::~Studio() {
    clear();
}

void Studio::clear() {
    for(unsigned int j=0; j<actionsLog.size(); j++) {
        delete actionsLog[j];
    }
    for(int i=0; i<getNumOfTrainers(); i++){
        delete trainers[i];
    }
    actionsLog.clear();
    trainers.clear();
    workout_options.clear();
}
//--------------------------------//
Studio::Studio(const std::string &configFilePath):open(false), trainers(), workout_options(), actionsLog(), customerId(0) {
    std::string line;
    std::ifstream thisFIle(configFilePath);

    int lineNum = 0;
    int numTrainers=0;
    int workId = 0;
    while (getline(thisFIle, line)) {
        if (line.empty() || line[0] == '#')
            continue;
        if (lineNum == 0) {
            size_t i = 0;
            while (i < line.size()) {  //number of trainers
                i++;
            }
            numTrainers = std::stoi(line.substr(0, i));
        }
        if (lineNum == 1) {
            for(int j=0; j<numTrainers; j++) { //capacity line
                int trainS = 0;
                int trainE = line.find(',');
                int cap = std::stoi(line.substr(trainS, trainE));
                Trainer *tmp = new Trainer(cap);
                tmp->setId(j);
                trainers.push_back(tmp);
                tmp = nullptr;
            }
        }
        else if(lineNum>1){
            int lineS = 0;
            int lineE;
            lineE = line.find(',');
            std::string name = line.substr(lineS, lineE);
            lineS = lineE+2;
            lineE = line.find(',',lineS);
            std::string type = line.substr(lineS,lineE-lineS);
            lineS = lineE+2;
            int price = std::stoi(line.substr(lineS, line.size()));
            if (type[0] == 'A') {
                Workout w =  Workout(workId, name, price, ANAEROBIC);
                workout_options.push_back(w);
            }
            else if (type[0] == 'M') {
                Workout w = Workout(workId, name, price, MIXED);
                workout_options.push_back(w);
            }
            else if (type[0] == 'C') {
                Workout w = Workout(workId, name, price, CARDIO);
                workout_options.push_back(w);
            }
            workId++;
        }
        lineNum++;
    }
}

void Studio:: start() {
    open = true;
    std::cout << "Studio is now open!" << std::endl;
    std::string input;
    while (open) {
        getline(std::cin, input);
        std::vector<Customer*> customList;

        if (input.find("open") == 0) {
            int idStart = input.find(" ");
            int idFinish = input.find(" ", idStart + 1);
            size_t id = std::stoi(input.substr(idStart, idFinish)); //changes string to int
            input = input.substr(idFinish + 1); //cut the input string from after the trainer's number
            std::string custom;
            std::string type;
            size_t i=0;
            if(id < trainers.size()) {
                size_t thisCapacity =getTrainer(id)->getCapacity();
                while (i < input.size() && customList.size() < thisCapacity) {
                    while (input[i] != ',') {
                        custom.push_back(input[i]);
                        i++;
                    }
                    i++;
                    while (i < input.size() && type.size() < 3) {
                        type.push_back(input[i]);
                        i++;
                    }
                    i++;
                    if (type == "swt") {
                        SweatyCustomer *swt = new SweatyCustomer(custom, customerId);
                        customList.push_back(swt);
                        customerId++;
                        swt = nullptr;
                    } else if (type == "chp") {
                        CheapCustomer *chp = new CheapCustomer(custom, customerId);
                        customList.push_back(chp);
                        customerId++;
                    } else if (type == "mcl") {
                        HeavyMuscleCustomer *mcl = new HeavyMuscleCustomer(custom, customerId);
                        customList.push_back(mcl);
                        customerId++;
                    } else if (type == "fbd") {
                        FullBodyCustomer *fbd = new FullBodyCustomer(custom, customerId);
                        customList.push_back(fbd);
                        customerId++;
                    }
                    custom = "";
                    type = "";
                }
            }
            OpenTrainer *thisOpen = new OpenTrainer(id, customList);
            thisOpen->act(*this);
            actionsLog.push_back(thisOpen);
        }

        if(input.find("order") == 0) {
            int idStart = input.find(" ");
            int idFinish = input.find(" ", idStart + 1);
            int id = std::stoi(input.substr(idStart, idFinish));
            input = input.substr(idFinish + 1);
            Order *thisOrder = new Order(id);
            thisOrder->act(*this);
            actionsLog.push_back(thisOrder);
        }

        if(input.find("move") == 0) {
            int startSource = input.find(" ");
            int finishSource = input.find(" ", startSource + 1);
            int source = std::stoi(input.substr(startSource, finishSource));
            input = input.substr(finishSource + 1);

            int startDest= 0;
            int finishDest = input.find(" ", startDest + 1);
            int dest = std::stoi(input.substr(startDest, finishDest));
            input = input.substr(finishDest + 1);

            int startCust = 0;
            int finishCust = input.find(" ", startCust + 1);
            int customer = std::stoi(input.substr(startCust, finishCust));
            input = input.substr(finishCust + 1);

            MoveCustomer* moveAction = new MoveCustomer(source, dest, customer);
            moveAction->act(*this);
            actionsLog.push_back(moveAction);
        }

        if(input.find("status") == 0) {
            int idStart = input.find(" ");
            int idFinish = input.find(" ", idStart + 1);
            int id = std::stoi(input.substr(idStart, idFinish));
            PrintTrainerStatus *pts =new  PrintTrainerStatus(id);
            pts->act(*this);
            actionsLog.push_back(pts);
        }
        if(input.find("close ") == 0) {
            int idStart = input.find(" ");
            int idFinish = input.find(" ", idStart + 1);
            int id = std::stoi(input.substr(idStart, idFinish));
            Close *close = new Close(id);
            close->act(*this);
            actionsLog.push_back(close);
        }
        if(input.find("closeall") == 0) {
            CloseAll *closeAll = new CloseAll();
            closeAll->act(*this);
            actionsLog.push_back(closeAll);
            open = false;
            for(auto action: actionsLog){
                if(action != nullptr) {
                    delete action;
                }
                actionsLog.clear();
            }
            for(auto train : trainers) {
                if(train!= nullptr) {
                    delete train;
                }
                trainers.clear();
            }
        }
        if(input.find("workout_options") == 0) {
            PrintWorkoutOptions *workOpt = new PrintWorkoutOptions();
            workOpt->act(*this);
            actionsLog.push_back(workOpt);
        }
        if(input.find("log") == 0) {
            PrintActionsLog *actionLg = new PrintActionsLog();
            actionLg->act(*this);
            actionsLog.push_back(actionLg);
        }
        if(input.find("backup") ==0) {
            BackupStudio *backUp = new BackupStudio();
            backUp->act(*this);
            actionsLog.push_back(backUp);
        }
        if(input.find("restore") ==0){
            RestoreStudio *restored = new RestoreStudio();
            restored->act(*this);
            actionsLog.push_back(restored);
        }

    }
}

int Studio:: getNumOfTrainers() const{
    return trainers.size();
}

Trainer* Studio:: getTrainer(int tid){
    for (Trainer* trainer: trainers) {
        if (trainer->getId() == tid)
            return trainer;
    }
    return nullptr;  //if the trainer was not found
}

const std::vector<BaseAction*>& Studio:: getActionsLog() const{
    return actionsLog;
}

std::vector<Workout>& Studio:: getWorkoutOptions() {
    return workout_options;
}

std::vector<Trainer*> Studio:: getAllTrainers() {
    return trainers;
}
void Studio::setOpen(bool isOpen) {
    open = isOpen;
    if(!isOpen) {
        workout_options.clear();
    }
}








