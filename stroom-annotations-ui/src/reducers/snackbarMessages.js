import {
    ACKNOWLEDGE_SNACKBAR,
    SEND_TO_SNACKBAR
} from '../actions/snackBar'

const defaultState = []

let messageId = 0

const snackbarMessages = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case SEND_TO_SNACKBAR:
            const newState = [
                ...state,
                {
                    messageId,
                    message: action.message
                }
            ]
            messageId += 1
            return newState

        case ACKNOWLEDGE_SNACKBAR:
            return state.filter(message => action.id !== message.id)
        default:
            return state
    }
}

export default snackbarMessages