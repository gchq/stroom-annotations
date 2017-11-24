import {
    RECEIVE_UPDATE_ANNOTATION
} from '../actions/updateAnnotation'

import {
    RECEIVE_CREATE_ANNOTATION
} from '../actions/createAnnotation'

import {
    RECEIVE_REMOVE_ANNOTATION
} from '../actions/removeAnnotation'

import {
    ACKNOWLEDGE_SNACKBAR,
    GENERIC_SNACKBAR
} from '../actions/acknowledgeApiMessages'

const defaultState = []

let messageId = 0

const snackbarMessages = (
    state = defaultState,
    action
 ) => {
    const generateNewState = (message) => {
        const newState = [
            ...state,
            {
                messageId,
                message
            }
        ]
        messageId += 1
        return newState
    }

    switch(action.type) {
        case RECEIVE_UPDATE_ANNOTATION:
            return generateNewState("Annotation Updated")
        case RECEIVE_REMOVE_ANNOTATION:
            return generateNewState("Annotation Removed")
        case RECEIVE_CREATE_ANNOTATION:
            return generateNewState("Annotation Created")
        case GENERIC_SNACKBAR:
            return generateNewState(action.message)
        case ACKNOWLEDGE_SNACKBAR:
            return state.filter(message => action.id !== message.id)
        default:
            return state
    }
}

export default snackbarMessages