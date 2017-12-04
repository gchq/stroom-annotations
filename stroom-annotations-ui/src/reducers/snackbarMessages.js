import {
    RECEIVE_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION_FAILED
} from '../actions/updateAnnotation'

import {
    RECEIVE_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION_FAILED
} from '../actions/createAnnotation'

import {
    RECEIVE_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION_FAILED
} from '../actions/removeAnnotation'

import {
    ACKNOWLEDGE_SNACKBAR,
    GENERIC_SNACKBAR
} from '../actions/acknowledgeApiMessages'

import {
    RECEIVE_FETCH_ANNOTATION_FAILED
} from '../actions/fetchAnnotation'

import {
    RECEIVE_SEARCH_ANNOTATIONS_FAILED
} from '../actions/searchAnnotations'

import {
    GENERIC_ERROR
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
        case RECEIVE_UPDATE_ANNOTATION_FAILED:
            return generateNewState("Failed to Update Annotation " + action.message)
        case RECEIVE_REMOVE_ANNOTATION:
            return generateNewState("Annotation Removed")
        case RECEIVE_REMOVE_ANNOTATION_FAILED:
            return generateNewState("Failed to Remove Annotation " + action.message)
        case RECEIVE_CREATE_ANNOTATION:
            return generateNewState("Annotation Created")
        case RECEIVE_CREATE_ANNOTATION_FAILED:
            return generateNewState("Failed to Create Annotation " + action.message)
        case RECEIVE_FETCH_ANNOTATION_FAILED:
            return generateNewState("Failed to Fetch Annotations " + action.message)
        case RECEIVE_SEARCH_ANNOTATIONS_FAILED:
            return generateNewState("Failed to Search Annotations " + action.message)
        case GENERIC_SNACKBAR:
        case GENERIC_ERROR:
            return generateNewState(action.message)

        case ACKNOWLEDGE_SNACKBAR:
            return state.filter(message => action.id !== message.id)
        default:
            return state
    }
}

export default snackbarMessages