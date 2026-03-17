export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
    empty: boolean;
    pageable?: {
        offset: number;
        pageNumber: number;
        pageSize: number;
        paged: boolean;
        unpaged: boolean;
    };
    sort?: {
        empty: boolean;
        sorted: boolean;
        unsorted: boolean;
    };
    numberOfElements?: number;
}