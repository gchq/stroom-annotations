import {
    REQUEST_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION_FAILED
} from '../actions/updateAnnotation'

import {
    REQUEST_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION_FAILED
} from '../actions/createAnnotation'

import {
    REQUEST_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION_NOT_EXIST,
    RECEIVE_FETCH_ANNOTATION_FAILED
} from '../actions/fetchAnnotation'

import {
    REQUEST_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION_FAILED
} from '../actions/removeAnnotation'

import {
    REQUEST_FETCH_ANNOTATION_HISTORY,
    RECEIVE_FETCH_ANNOTATION_HISTORY,
    RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED
} from '../actions/fetchAnnotationHistory'

import {
    REQUEST_SEARCH_ANNOTATIONS,
    REQUEST_MORE_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS,
    RECEIVE_SEARCH_ANNOTATIONS_FAILED
} from '../actions/searchAnnotations'

import {
    REQUEST_FETCH_STATUS_VALUES,
    RECEIVE_FETCH_STATUS_VALUES,
    RECEIVE_FETCH_STATUS_VALUES_FAILED
} from '../actions/fetchStatusValues'

const defaultState = []

const apiCalls = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case REQUEST_FETCH_STATUS_VALUES:
        case REQUEST_UPDATE_ANNOTATION:
        case REQUEST_CREATE_ANNOTATION:
        case REQUEST_FETCH_ANNOTATION:
        case REQUEST_REMOVE_ANNOTATION:
        case REQUEST_FETCH_ANNOTATION_HISTORY:
        case REQUEST_SEARCH_ANNOTATIONS:
        case REQUEST_MORE_ANNOTATIONS:
            return [
                ...state,
                {
                    type: action.type,
                    apiCallId: action.apiCallId
                }
            ]
        case RECEIVE_FETCH_STATUS_VALUES:
        case RECEIVE_UPDATE_ANNOTATION:
        case RECEIVE_CREATE_ANNOTATION:
        case RECEIVE_FETCH_ANNOTATION:
        case RECEIVE_FETCH_ANNOTATION_NOT_EXIST:
        case RECEIVE_REMOVE_ANNOTATION:
        case RECEIVE_FETCH_ANNOTATION_HISTORY:
        case RECEIVE_SEARCH_ANNOTATIONS:
        case RECEIVE_FETCH_STATUS_VALUES_FAILED:
        case RECEIVE_UPDATE_ANNOTATION_FAILED:
        case RECEIVE_CREATE_ANNOTATION_FAILED:
        case RECEIVE_FETCH_ANNOTATION_FAILED:
        case RECEIVE_REMOVE_ANNOTATION_FAILED:
        case RECEIVE_FETCH_ANNOTATION_HISTORY_FAILED:
        case RECEIVE_SEARCH_ANNOTATIONS_FAILED:
            return state.filter(a => a.apiCallId !== action.apiCallId)
        default:
            return state
    }
}

export default apiCalls