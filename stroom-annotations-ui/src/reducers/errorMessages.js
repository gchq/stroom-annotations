import {
    RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED
} from '../actions/fetchAnnotationHistory'

import {
    RECEIVE_UPDATE_ANNOTATION_FAILED
} from '../actions/updateAnnotation'

import {
    RECEIVE_CREATE_ANNOTATION_FAILED
} from '../actions/createAnnotation'

import {
    RECEIVE_FETCH_ANNOTATION_FAILED
} from '../actions/fetchAnnotation'

import {
    RECEIVE_REMOVE_ANNOTATION_FAILED
} from '../actions/removeAnnotation'

import {
    RECEIVE_SEARCH_ANNOTATIONS_FAILED
} from '../actions/searchAnnotations'

import {
    ACKNOWLEDGE_ERROR,
    GENERIC_ERROR
} from '../actions/acknowledgeApiMessages'

const defaultState = []

let id = 0

const errorMessages = (
    state = defaultState,
    action
 ) => {
    const generateNewState = (userFriendlyType) => {
        const newState = [
            ...state,
            {
                id,
                action: userFriendlyType,
                message: action.message
            }
        ]
        id += 1
        return newState
    }

    switch(action.type) {

        case RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED:
            return generateNewState('Failed to Fetch Annotations')
        case RECEIVE_UPDATE_ANNOTATION_FAILED:
            return generateNewState('Failed to Update Annotation')
        case RECEIVE_CREATE_ANNOTATION_FAILED:
            return generateNewState('Failed to Create Annotation')
        case RECEIVE_FETCH_ANNOTATION_FAILED:
            return generateNewState('Failed to Fetch Annotation')
        case RECEIVE_REMOVE_ANNOTATION_FAILED:
            return generateNewState('Failed to Remove Annotation')
        case RECEIVE_SEARCH_ANNOTATIONS_FAILED:
            return generateNewState('Failed to Search Annotations')
        case GENERIC_ERROR:
            return generateNewState(action.message)
        case ACKNOWLEDGE_ERROR:
            return state.filter(error => action.id !== error.id)
        default:
            return state
    }
}

export default errorMessages